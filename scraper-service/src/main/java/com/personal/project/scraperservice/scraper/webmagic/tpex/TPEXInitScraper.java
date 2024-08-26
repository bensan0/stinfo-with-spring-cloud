package com.personal.project.scraperservice.scraper.webmagic.tpex;

import cn.hutool.core.util.StrUtil;
import com.personal.project.scraperservice.constant.Term;
import com.personal.project.scraperservice.model.dto.DailyStockInfoDTO;
import com.personal.project.scraperservice.model.entity.ScraperErrorMessageDO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class TPEXInitScraper implements PageProcessor {

    private final Site site = Site.me()
            .setDefaultCharset("utf-8")
            .setCycleRetryTimes(0)
            .setSleepTime(7000)
            .setUserAgent("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2.1 Safari/605.1.15")
            .addHeader("Accept-Language", "zh-TW,zh-Hant;q=0.9")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Encoding", "gzip, deflate, br");

    private final List<String> urls = new ArrayList<>();

    @Getter
    private final List<ScraperErrorMessageDO> errors = Collections.synchronizedList(new ArrayList<>());

    public TPEXInitScraper() {
    }

    public TPEXInitScraper(List<String> urls) {
        this.urls.addAll(urls);
    }

    @Override
    public void process(Page page) {
        page.addTargetRequests(urls);
        List<Selectable> rows = page.getHtml().xpath("/html/body/table/tbody/tr").nodes();
        String rawDate = page.getUrl().get().replace("https://www.tpex.org.tw/web/stock/aftertrading/otc_quotes_no1430/stk_wn1430_result.php?l=zh-tw&o=htm&d=", "")
                .replace("&se=AL&s=0,asc,0", "");

        String[] split = rawDate.trim().split("/");
        String year = String.valueOf(Long.parseLong(split[0]) + 1911);
        String date = year + split[1] + split[2];

        if (rows.size() == 0) {
            ScraperErrorMessageDO error = new ScraperErrorMessageDO();
            error.setDate(date);
            error.setErrorMessage("本日tpex無收盤行情");
            error.setScraperName("TPEX Routine Scraper");
            errors.add(error);
            page.setSkip(true);
            return;
        }

        List<DailyStockInfoDTO> results = new ArrayList<>();
        for (Selectable row : rows) {

            String stockId = row.xpath("//tr/td[1]/text()").get().trim();
            if (!StrUtil.isNumeric(stockId) || stockId.length() > 4) {
                continue;
            }

            DailyStockInfoDTO dto = new DailyStockInfoDTO();
            dto.setStockId(stockId);

            String stockName = row.xpath("//tr/td[2]/text()").get();
            dto.setStockName(stockName);
            dto.setMarket(Term.OTC.getFieldName());
            dto.setDate(Long.parseLong(date));

            //張
            dto.setTodayTradingVolumePiece(
                    new BigDecimal(row.xpath("//tr/td[8]/text()").get().trim().replace(",", ""))
                            .divide(BigDecimal.valueOf(1000), 2, RoundingMode.FLOOR)
                            .longValue()
            );

            //額
            dto.setTodayTradingVolumeMoney(
                    new BigDecimal(row.xpath("//tr/td[9]/text()").get().trim().replace(",", ""))
            );

            //開
            String rawOp = row.xpath("//tr/td[5]/text()").get().trim().replace(",", "");
            try {
                dto.setOpeningPrice(new BigDecimal(rawOp));
            } catch (Exception ignored) {
            }

            //高
            String rawHigh = row.xpath("//tr/td[6]/text()").get().trim().replace(",", "");
            try {
                dto.setHighestPrice(new BigDecimal(rawHigh));
            } catch (Exception ignored) {
            }

            //低
            String rawLow = row.xpath("//tr/td[7]/text()").get().trim().replace(",", "");
            try {
                dto.setLowestPrice(new BigDecimal(rawLow));
            } catch (Exception ignored) {
            }

            //收
            String rawClose = row.xpath("//tr/td[3]/text()").get().trim().replace(",", "");
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

    public void setUrls(List<String> urls) {
        this.urls.addAll(urls);
    }

    public String getUrlsFirst() {
        return urls.getFirst();
    }
}
