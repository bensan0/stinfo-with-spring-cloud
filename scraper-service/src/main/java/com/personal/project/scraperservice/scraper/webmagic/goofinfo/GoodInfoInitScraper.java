package com.personal.project.scraperservice.scraper.webmagic.goofinfo;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.personal.project.scraperservice.model.dto.DailyStockInfoDto;
import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GoodInfoInitScraper implements PageProcessor {

    private Site site = Site.me()
            .setDefaultCharset("utf-8")
//            .setRetryTimes(3)
            .setCycleRetryTimes(0)
            .setSleepTime(5000)
            .setUserAgent("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2.1 Safari/605.1.15")
            .addHeader("Accept-Language", "zh-TW,zh-Hant;q=0.9")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Encoding", "gzip, deflate, br");

    @Override
    public void process(Page page) {
        Html html = page.getHtml();
        if(StrUtil.isEmpty(html.xpath("//a[@class='link_blue' and @style='font-size:14pt;font-weight:bold;']/text()").get())){
            page.setSkip(true);
            page.addTargetRequests(getTargetUrls(html));

            return;
        }

        String[] idName = html.xpath("//a[@class='link_blue' and @style='font-size:14pt;font-weight:bold;']/text()").get().split(" +");
        if(!isTargetScrapePage(idName[0])){
            page.setSkip(true);
            page.addTargetRequests(getTargetUrls(html));

            return;
        }

        LocalDateTime today = LocalDateTime.now();
        int year = today.getYear();
        String yearStr = String.valueOf(year);
//        String startDate = html.xpath("//input[@id='edtSTART_DT']/@value").get();

        //獲取資料列
        //todo 錯誤處理
        List<Selectable> rows = html.xpath("//table[@id='tblPriceDetail']/tbody/tr").nodes();
        List<DailyStockInfoDto> dtos = new ArrayList<>();
        DailyStockInfoDto tempDTO = null;//暫存上一個DTO, 下一輪填充該DTO昨日資訊
        for (int i = 0; i < rows.size(); i++) {
            Selectable tr = rows.get(i);
            if (!StrUtil.isEmpty(tr.xpath("//tr/@id").get())) {
                DailyStockInfoDto dto = new DailyStockInfoDto();
                dto.setStockId(idName[0]);
                dto.setStockName(idName[1]);

                //日期 todo 優化判斷, 萬一有到前年
                String date = "19891111";
                String[] yyMMdd = tr.xpath("//tr/td[1]/nobr/text()").get().replace("'", "").split("/");
                if (yyMMdd[0].equals(yearStr.substring(2, 4))) {
                    date = StrUtil.format("{}{}{}", String.valueOf(year), yyMMdd[1], yyMMdd[2]);
                } else if (yyMMdd[0].equals(String.valueOf(year - 1).substring(2, 4))) {
                    date = StrUtil.format("{}{}{}", String.valueOf(year - 1), yyMMdd[1], yyMMdd[2]);
                }
                dto.setDate(Long.valueOf(date));
                //比較本輪日期與tempDTO日期, 若本輪日期非DTO.date - 1day, 則開始檢查loop DTO.date -1 , DTO.date -2 ...直至本輪日期後一日
                //若為六日則不做處置, 若非為六日, 則map新增日期的空資料物件
                if (tempDTO != null && isNotYesterday(tempDTO.getDate(), date)) {
                    LocalDate tempDate = LocalDate.parse(tempDTO.getDate().toString(), DatePattern.PURE_DATE_FORMATTER).minusDays(1);
                    while (!tempDate.format(DatePattern.PURE_DATE_FORMATTER).equals(date)) {
                        if (isNotWeekend(tempDate)) {
                            DailyStockInfoDto emptyDTO = new DailyStockInfoDto();
                            emptyDTO.setStockId(idName[0]);
                            emptyDTO.setStockName(idName[1]);
                            emptyDTO.setDate(Long.valueOf(tempDate.format(DatePattern.PURE_DATE_FORMATTER)));
                            dtos.add(emptyDTO);
                        }
                        tempDate = tempDate.minusDays(1);
                    }
                }

                //開盤
                BigDecimal openingPrice = new BigDecimal(tr.xpath("//tr/td[2]/nobr/text()").get());
                dto.setOpeningPrice(openingPrice);

                //高
                BigDecimal highestPrice = new BigDecimal(tr.xpath("//tr/td[3]/nobr/text()").get());
                dto.setHighestPrice(highestPrice);

                //低
                BigDecimal lowestPrice = new BigDecimal(tr.xpath("//tr/td[4]/nobr/text()").get());
                dto.setLowestPrice(lowestPrice);

                //收
                BigDecimal todayClosingPrice = new BigDecimal(tr.xpath("//tr/td[5]/nobr/text()").get());
                dto.setTodayClosingPrice(todayClosingPrice);

                //漲跌
                BigDecimal priceGap = new BigDecimal(tr.xpath("//tr/td[6]/nobr/text()").get());
                dto.setPriceGap(priceGap);

                //漲跌幅
                BigDecimal priceGapPercent = new BigDecimal(tr.xpath("//tr/td[7]/nobr/text()").get());
                dto.setPriceGapPercent(priceGapPercent);

                //張數
                Long todayTradingVolumePiece = null;
                if (!StrUtil.isEmpty(tr.xpath("//tr/td[9]/nobr/text()").get())) {
                    todayTradingVolumePiece = new BigDecimal(tr.xpath("//tr/td[9]/nobr/text()").get().replace(",", "")).longValue();
                }
                dto.setTodayTradingVolumePiece(todayTradingVolumePiece);

                //交易額(億元)
                BigDecimal todayTradingVolumeMoney = null;
                if (!StrUtil.isEmpty(tr.xpath("//tr/td[12]/nobr/text()").get())) {
                    todayTradingVolumeMoney = new BigDecimal(tr.xpath("//tr/td[12]/nobr/text()").get().replace(",", "")).multiply(new BigDecimal(100000000));
                }
                dto.setTodayTradingVolumeMoney(todayTradingVolumeMoney);

                //缺：昨收 昨張 昨額
                if (tempDTO != null) {
                    tempDTO.setYesterdayClosingPrice(todayClosingPrice);
                    tempDTO.setYesterdayTradingVolumePiece(todayTradingVolumePiece);
                    tempDTO.setYesterdayTradingVolumeMoney(todayTradingVolumeMoney);
                }

                tempDTO = dto;
                dtos.add(dto);
            }
        }
        page.putField("DTOs", dtos);
        page.addTargetRequests(getTargetUrls(html));
    }

    @Override
    public Site getSite() {
        return site;
    }

    private boolean isNotYesterday(Long newerDateNumber, String olderDateStr) {
        LocalDate newerDate = LocalDate.parse(newerDateNumber.toString(), DatePattern.PURE_DATE_FORMATTER);
        return (newerDate.minusDays(1L).toString().equals(
                LocalDate.parse(olderDateStr, DatePattern.PURE_DATE_FORMATTER).toString())
        );
    }

    private boolean isNotWeekend(LocalDate date) {
        return date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY;
    }

    private List<String> getTargetUrls(Html html) {
        List<String> all = html.links().regex("(https:\\/\\/goodinfo\\.tw\\/tw\\/ShowK_Chart\\.asp\\?STOCK_ID=[0-9a-zA-Z]*)").all();
        List<String> newUrls = new ArrayList<>();
        for (String url : all) {
            if (url.contains("&INITIALIZED=T")) {
                newUrls.add(url.replace("&INITIALIZED=T", ""));
            } else {
                newUrls.add(url);
            }
        }
        return newUrls;
    }

    private boolean isTargetScrapePage(String stockId){

        return stockId.length() == 4 && !stockId.startsWith("0") && StrUtil.isNumeric(stockId);
    }
}
