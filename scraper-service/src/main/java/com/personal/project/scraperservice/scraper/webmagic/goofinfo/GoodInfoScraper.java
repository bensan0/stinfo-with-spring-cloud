package com.personal.project.scraperservice.scraper.webmagic.goofinfo;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.personal.project.scraperservice.model.dto.DailyStockInfoDto;
import com.personal.project.scraperservice.model.entity.ScraperErrorMessageDO;
import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class GoodInfoScraper implements PageProcessor {

    private Site site = Site.me()
            .setDefaultCharset("utf-8")
            .setCycleRetryTimes(0)
            .setSleepTime(3000)
            .setUserAgent("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2.1 Safari/605.1.15")
            .addHeader("Accept-Language", "zh-TW,zh-Hant;q=0.9")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Encoding", "gzip, deflate, br");

    @Override
    public void process(Page page) {
        LocalDate now = LocalDate.now();
        Html html = page.getHtml();
        List<DailyStockInfoDto> dtos = new ArrayList<>();
        List<ScraperErrorMessageDO> errors = new ArrayList<>();
        List<Selectable> trs = html.xpath("//table[@id='tblStockList']/tbody/tr").nodes();
        for (Selectable tr : trs) {
            try {
                if (!StrUtil.isEmpty(tr.xpath("//tr/@id").get())) {
                    String stockId = tr.xpath("//tr/td[1]/nobr/a/text()").get();
                    if (!StrUtil.isNumeric(stockId) && stockId.length() != 4) {
                        continue;
                    }

                    String stockName = tr.xpath("//tr/td[2]/nobr/a/text()").get();

                    if (!validDate(now, tr.xpath("//tr/td[4]/nobr/text()").get())) {
                        DailyStockInfoDto dto = new DailyStockInfoDto();
                        dto.setStockId(stockId);
                        dto.setStockName(stockName);
                        dto.setDate(Long.parseLong(now.format(DatePattern.PURE_DATE_FORMATTER)));
                        dtos.add(dto);
                        continue;
                    }

                    Long date = generateDate(now, tr.xpath("//tr/td[4]/nobr/text()").get());

                    //收
                    BigDecimal closingPrice = new BigDecimal(
                            tr.xpath("//tr/td[6]/nobr/text()").get().trim().replace(",", "")
                    );

                    //漲跌價
                    BigDecimal priceGap = new BigDecimal(
                            tr.xpath("//tr/td[7]/nobr/text()").get().trim().replace(",", "")
                    );

                    //漲跌幅
                    BigDecimal priceGapPercent = new BigDecimal(
                            tr.xpath("//tr/td[8]/nobr/text()").get().trim().replace(",", "")
                    );

                    //張
                    Long tradingVolumePiece = Long.parseLong(
                            tr.xpath("//tr/td[9]/nobr/text()").get().trim().replace(",", "")
                    );

                    //百萬
                    BigDecimal tradingVolumeMoney = new BigDecimal(
                            tr.xpath("//tr/td[10]/nobr/text()").get().trim().replace(",", "")
                    ).multiply(BigDecimal.valueOf(1000000));

                    //昨收
                    BigDecimal yesterdayClosingPrice = new BigDecimal(
                            tr.xpath("//tr/td[11]/nobr/text()").get().trim().replace(",", "")
                    );

                    //開
                    BigDecimal openingPrice = new BigDecimal(
                            tr.xpath("//tr/td[12]/nobr/text()").get().trim().replace(",", "")
                    );

                    //高
                    BigDecimal highestPrice = new BigDecimal(
                            tr.xpath("//tr/td[13]/nobr/text()").get().trim().replace(",", "")
                    );

                    //低
                    BigDecimal lowestPrice = new BigDecimal(
                            tr.xpath("//tr/td[14]/nobr/text()").get().trim().replace(",", "")
                    );

                    //缺昨張 昨額

                    DailyStockInfoDto dto = new DailyStockInfoDto();
                    dto.setStockId(stockId);
                    dto.setStockName(stockName);
                    dto.setDate(date);
                    dto.setOpeningPrice(openingPrice);
                    dto.setHighestPrice(highestPrice);
                    dto.setLowestPrice(lowestPrice);
                    dto.setTodayClosingPrice(closingPrice);
                    dto.setPriceGap(priceGap);
                    dto.setPriceGapPercent(priceGapPercent);
                    dto.setYesterdayClosingPrice(yesterdayClosingPrice);
                    dto.setTodayTradingVolumePiece(tradingVolumePiece);
                    dto.setTodayTradingVolumeMoney(tradingVolumeMoney);
                    dtos.add(dto);
                }

            } catch (Exception e) {
                ScraperErrorMessageDO error = new ScraperErrorMessageDO();
                error.setErrorMessage("GoodInfo scraper 爬蟲出現錯誤");
                error.setDate(LocalDate.now().toString());
                error.setException(e.getClass().getSimpleName());
                error.setExceptionMessage(e.getMessage());
                error.setExtra(Arrays.toString(e.getCause().getStackTrace()));
                errors.add(error);
            }

        }

        page.putField("dtos", dtos);
        page.putField("errors", errors);
    }

    @Override
    public Site getSite() {
        return site;
    }

    private boolean validDate(LocalDate now, String content) {
        try {
            String[] monthDate = content.split("/");

            return now.getMonth().getValue() == Integer.parseInt(monthDate[0].trim()) && now.getDayOfMonth() == Integer.parseInt(monthDate[1].trim());
        } catch (Exception e) {
            log.error("GoodInfoScraper valid date go wrong, content: {}", content);
            return false;
        }
    }

    private Long generateDate(LocalDate now, String content) {
        String[] monthDate = content.split("/");
        String date = LocalDate.of(now.getYear(), Integer.parseInt(monthDate[0].trim()), Integer.parseInt(monthDate[1].trim())).format(DatePattern.PURE_DATE_FORMATTER);

        return Long.parseLong(date);
    }
}
