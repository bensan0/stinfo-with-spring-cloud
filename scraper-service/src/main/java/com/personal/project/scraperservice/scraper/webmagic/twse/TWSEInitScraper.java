package com.personal.project.scraperservice.scraper.webmagic.twse;

import cn.hutool.core.util.StrUtil;
import com.personal.project.scraperservice.constant.Term;
import com.personal.project.scraperservice.model.dto.DailyStockInfoDTO;
import com.personal.project.scraperservice.model.entity.ScraperErrorMessageDO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class TWSEInitScraper implements PageProcessor {

    private final Site site = Site.me()
            .setDefaultCharset("utf-8")
            .setCycleRetryTimes(0)
            .setSleepTime(5000)
            .setUserAgent("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2.1 Safari/605.1.15")
            .addHeader("Accept-Language", "zh-TW,zh-Hant;q=0.9")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Encoding", "gzip, deflate, br");

    private final List<String> urls = new ArrayList<>();

    @Getter
    private final List<ScraperErrorMessageDO> errors = Collections.synchronizedList(new ArrayList<>());

    public TWSEInitScraper() {
    }

    public TWSEInitScraper(List<String> urls) {
        this.urls.addAll(urls);
    }

    @Override
    public void process(Page page) {
        page.addTargetRequests(urls);
        Html html = page.getHtml();
        String date = page.getUrl().get().replace("https://www.twse.com.tw/rwd/zh/afterTrading/MI_INDEX?date=", "").replace("&type=ALL&response=html", "");
        List<Selectable> nodes = html.xpath("//table").nodes();
        Selectable targetTable = nodes.stream()
                .filter(s -> s.xpath("//table/thead/tr/th/div/text()").get().contains("每日收盤行情(全部)"))
                .findFirst()
                .orElse(null);

        if (targetTable == null) {
            ScraperErrorMessageDO error = new ScraperErrorMessageDO();
            error.setDate(date);
            error.setErrorMessage("本日twse無收盤行情");
            error.setScraperName("TWSE Routine Scraper");
            errors.add(error);
            page.setSkip(true);
            return;
        }

        List<Selectable> rows = targetTable.xpath("//table/tbody/tr").nodes();

        List<DailyStockInfoDTO> results = new ArrayList<>();
        for (Selectable row : rows) {
            String stockId = row.xpath("//tr/td[1]/text()").get();
            if (stockId.trim().length() > 4 || !StrUtil.isNumeric(stockId.trim())) {
                continue;
            }

            DailyStockInfoDTO dto = new DailyStockInfoDTO();
            dto.setStockId(stockId);

            String stockName = row.xpath("//tr/td[2]/text()").get();
            dto.setStockName(stockName);
            dto.setMarket(Term.LISTED.getFieldName());
            dto.setDate(Long.parseLong(date));

            //張
            Long tradingPiece = new BigDecimal(
                    row.xpath("//tr/td[3]/text()").get().trim().replace(",", "")
            ).divide(BigDecimal.valueOf(1000), 2, RoundingMode.FLOOR).longValue();
            dto.setTodayTradingVolumePiece(tradingPiece);

            //額
            BigDecimal tradingAmount = new BigDecimal(
                    row.xpath("//tr/td[5]/text()").get().trim().replace(",", "")
            );
            dto.setTodayTradingVolumeMoney(tradingAmount);

            //開
            String rawOp = row.xpath("//tr/td[6]/text()").get().trim().replace(",", "");
            try {
                dto.setOpeningPrice(new BigDecimal(rawOp));
            } catch (Exception ignored) {
            }

            //高
            String rawHigh = row.xpath("//tr/td[7]/text()").get().trim().replace(",", "");
            try {
                dto.setHighestPrice(new BigDecimal(rawHigh));
            } catch (Exception ignored) {
            }

            //低
            String rawLow = row.xpath("//tr/td[8]/text()").get().trim().replace(",", "");
            try {
                dto.setLowestPrice(new BigDecimal(rawLow));
            } catch (Exception ignored) {
            }

            //收
            String rawClose = row.xpath("//tr/td[9]/text()").get().trim().replace(",", "");
            try {
                dto.setTodayClosingPrice(new BigDecimal(rawClose));
            } catch (Exception ignored) {
            }

            results.add(dto);
        }

        page.putField("dtos", results);
    }

    @Override
    public Site getSite() {
        return site;
    }

    public void setUrls(List<String> urls){
        this.urls.addAll(urls);
    }

    public String getFirstUrl(){
        return urls.getFirst();
    }
}
