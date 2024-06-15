package com.personal.project.scraperservice.scraper.webmagic.tpex;

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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TPEXInitScraper implements PageProcessor {

    private final Site site = Site.me()
            .setDefaultCharset("utf-8")
            .setCycleRetryTimes(0)
            .setSleepTime(5000)
            .setUserAgent("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2.1 Safari/605.1.15")
            .addHeader("Accept-Language", "zh-TW,zh-Hant;q=0.9")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Encoding", "gzip, deflate, br");

    private final List<String> urls = new ArrayList<>();

    public TPEXInitScraper() {
    }

    public TPEXInitScraper(List<String> urls) {
        this.urls.addAll(urls);
    }

    @Override
    public void process(Page page) {
        List<ScraperErrorMessageDO> errors = new ArrayList<>();
        try {
            Html html = page.getHtml();
            List<Selectable> trs = html.xpath("/html/body/table/tbody/tr").nodes();
            String[] split = html.xpath("//th[@id='th']/text()").get().split(" +");
            String stockId = split[1].split(":")[1].trim();
            String stockName = split[2].split(":")[1].trim();
            List<DailyStockInfoDto> dtos = new ArrayList<>();
            for (int i = 0; i < trs.size(); i++) {
                Selectable tr = trs.get(i);
                DailyStockInfoDto dto = new DailyStockInfoDto();
                dto.setStockId(stockId);
                dto.setStockName(stockName);

                //日
                String[] yMd = tr.xpath("//tr/td[1]/text()").get().split("/");
                LocalDate localDate = LocalDate.of(Integer.valueOf(yMd[0]) + 1911, Integer.valueOf(yMd[1]), Integer.valueOf(yMd[2]));
                dto.setDate(Long.valueOf(localDate.format(DatePattern.PURE_DATE_FORMATTER)));

                //股數
                Long tradingVolumePiece = new BigDecimal(tr.xpath("//tr/td[2]/text()").get().replace(",", "").trim()).longValue();
                dto.setTodayTradingVolumePiece(tradingVolumePiece);

                //額
                BigDecimal tradingVolumeMoney = new BigDecimal(tr.xpath("//tr/td[3]/text()").get().replace(",", "").trim()).multiply(BigDecimal.valueOf(1000));
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

                //差額
                BigDecimal priceGap = new BigDecimal(tr.xpath("//tr/td[8]/text()").get().replace(",", "").trim());
                dto.setPriceGap(priceGap);

                //昨收
                dto.setYesterdayClosingPrice(closingPrice.subtract(priceGap));

                //漲跌幅
                dto.setPriceGapPercent(
                        priceGap.divide(dto.getYesterdayClosingPrice(), 4, RoundingMode.FLOOR).multiply(BigDecimal.valueOf(100))
                );

                dtos.add(dto);
            }

            page.putField("idToDTOs", Pair.of(stockId, dtos));
            page.addTargetRequests(urls);
        } catch (Exception e) {
            ScraperErrorMessageDO error = new ScraperErrorMessageDO();
            error.setErrorMessage(StrUtil.format("TPEX init scraper error"));
            error.setException(e.getClass().getSimpleName());
            error.setExceptionMessage(e.getMessage());
            error.setScraperName("TPEX init scraper");
            error.setDate(LocalDate.now().format(DatePattern.PURE_DATE_FORMATTER));
            errors.add(error);

            log.error("TPEX init scraper error", e);

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
