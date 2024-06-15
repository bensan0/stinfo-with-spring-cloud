package com.personal.project.scraperservice.scraper.webmagic.twse;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.personal.project.scraperservice.model.dto.DailyStockInfoDto;
import com.personal.project.scraperservice.model.entity.ScraperErrorMessageDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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

    public TWSEInitScraper() {
    }

    public TWSEInitScraper(List<String> urls) {
        this.urls.addAll(urls);
    }

    @Override
    public void process(Page page) {
        List<ScraperErrorMessageDO> errors = new ArrayList<>();
        try {
            Html html = page.getHtml();
            List<Selectable> trs = html.xpath("/html/body/div/table/tbody/tr").nodes();
            String[] split = html.xpath("/html/body/div/table/thead/tr[1]/th/div/text()").get().split(" +");
            List<DailyStockInfoDto> dtos = new ArrayList<>();
            for (int i = 0; i < trs.size(); i++) {
                Selectable tr = trs.get(i);
                DailyStockInfoDto dto = new DailyStockInfoDto();
                dto.setStockId(split[1]);
                dto.setStockName(split[2]);
                //日
                String[] yMd = tr.xpath("//tr/td[1]/text()").get().split("/");
                LocalDate localDate = LocalDate.of(Integer.valueOf(yMd[0]) + 1911, Integer.valueOf(yMd[1]), Integer.valueOf(yMd[2]));
                dto.setDate(Long.valueOf(localDate.format(DatePattern.PURE_DATE_FORMATTER)));

                //股數
                Long tradingVolumePiece = new BigDecimal(tr.xpath("//tr/td[2]/text()").get().replace(",", "").trim()).divide(BigDecimal.valueOf(1000)).longValue();
                dto.setTodayTradingVolumePiece(tradingVolumePiece);

                //額
                BigDecimal tradingVolumeMoney = new BigDecimal(tr.xpath("//tr/td[3]/text()").get().replace(",", "").trim());
                dto.setTodayTradingVolumeMoney(tradingVolumeMoney);

                //開
                BigDecimal openingPrice = new BigDecimal(tr.xpath("//tr/td[4]/text()").get().replace(",", "").trim());
                dto.setOpeningPrice(openingPrice);

                //高
                BigDecimal highestPrice = new BigDecimal(tr.xpath("//tr/td[5]/text()").get().replace(",", "").trim());
                dto.setHighestPrice(highestPrice);

                //低
                BigDecimal lowestPrice = new BigDecimal(tr.xpath("//tr/td[6]/text()").get().replace(",", "").trim());
                dto.setLowestPrice(lowestPrice);

                //收
                BigDecimal closingPrice = new BigDecimal(tr.xpath("//tr/td[7]/text()").get().replace(",", "").trim());
                dto.setTodayClosingPrice(closingPrice);

                //缺的昨額 昨收 昨量 差額 漲跌幅在job邏輯處理

                dtos.add(dto);
            }

            page.putField("idToDTOs", Pair.of(split[1], dtos));
            page.addTargetRequests(urls);
        } catch (Exception e) {
            ScraperErrorMessageDO error = new ScraperErrorMessageDO();
            error.setErrorMessage(StrUtil.format("TWSE init scraper error"));
            error.setException(e.getClass().getSimpleName());
            error.setExceptionMessage(e.getMessage());
            error.setScraperName("TWSE init scraper");
            error.setDate(LocalDate.now().format(DatePattern.PURE_DATE_FORMATTER));
            errors.add(error);

            log.error("TWSE init scraper error", e);

            page.putField("errors", errors);
        }
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
