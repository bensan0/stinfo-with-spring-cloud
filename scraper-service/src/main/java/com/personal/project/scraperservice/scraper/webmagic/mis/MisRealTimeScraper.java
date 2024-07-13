package com.personal.project.scraperservice.scraper.webmagic.mis;

import com.personal.project.scraperservice.model.dto.DailyStockInfoDto;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MisRealTimeScraper implements PageProcessor {

    private final Site site = Site.me()
            .setDefaultCharset("utf-8")
            .setCycleRetryTimes(0)
            .setSleepTime(3000)
            .setUserAgent("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2.1 Safari/605.1.15")
            .addHeader("Accept-Language", "zh-TW,zh-Hant;q=0.9")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Encoding", "gzip, deflate, br");

    private final List<String> urls;

    private final Long date;

    public MisRealTimeScraper(List<String> urls, Long date) {
        this.urls = urls;
        this.date = date;
    }

    @Override
    public void process(Page page) {
        page.addTargetRequests(urls);
        Html html = page.getHtml();
        List<Selectable> rows = html.xpath("//table[@id='group']/tr").nodes();

        List<DailyStockInfoDto> results = new ArrayList<>();

        for (Selectable row : rows) {
            DailyStockInfoDto dto = new DailyStockInfoDto();
            String[] split = row.xpath("//tr/td[1]/a/text()").get().split(" +");
            String stockId = split[0].trim();
            dto.setStockId(stockId);
            String stockName = split[1].trim();
            dto.setStockName(stockName);
            dto.setDate(date);

            //最近成交
            String rawDeal = row.xpath("//tr/td[2]/text()").get().trim();
            try {
                dto.setTodayClosingPrice(new BigDecimal(rawDeal));
            } catch (Exception e) {
                continue;
            }

            //漲跌幅
            String rawPgp = row.xpath("//tr/td[3]/label[2]/text()").get().replace("(", "").replace(")", "").trim();
            try {
                dto.setPriceGapPercent(new BigDecimal(rawPgp));
            } catch (Exception e) {
                dto.setPriceGapPercent(BigDecimal.ZERO);
            }


            //漲跌
            String rawPg = row.xpath("//tr/td[3]/label[1]/text()").get().replace("▲", "").replace("▼", "").trim();
            try {
                if (dto.getPriceGapPercent().compareTo(BigDecimal.ZERO) > 0) {
                    dto.setPriceGap(new BigDecimal(rawPg));
                } else if (dto.getPriceGapPercent().compareTo(BigDecimal.ZERO) == 0) {
                    dto.setPriceGap(BigDecimal.ZERO);
                } else {
                    dto.setPriceGap(BigDecimal.ZERO.subtract(new BigDecimal(rawPg)));
                }
            } catch (Exception e) {
                dto.setPriceGap(BigDecimal.ZERO);
            }

            //開
            String rawOp = row.xpath("//tr/td[8]/text()").get().trim();
            dto.setOpeningPrice(new BigDecimal(rawOp));

            //高
            String rawHigh = row.xpath("//tr/td[9]/text()").get().trim();
            dto.setHighestPrice(new BigDecimal(rawHigh));

            //低
            String rawLow = row.xpath("//tr/td[10]/text()").get().trim();
            dto.setLowestPrice(new BigDecimal(rawLow));

            //累積成交量(張)
            String rawVol = row.xpath("//tr/td[5]/text()").get().trim();
            dto.setTodayTradingVolumePiece(Long.parseLong(rawVol));

            results.add(dto);
        }

        page.putField("dtos", results);
    }

    @Override
    public Site getSite() {
        return site;
    }

    public String getFirstUrl() {
        return urls.getFirst();
    }
}
