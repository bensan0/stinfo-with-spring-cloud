package com.personal.project.scraperservice.scraper.webmagic.goofinfo;

import com.personal.project.scraperservice.constant.Term;
import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class GoodInfoScraper implements PageProcessor {

    private Site site = Site.me()
            .setDefaultCharset("utf-8")
            .setRetryTimes(3)
            .setCycleRetryTimes(3)
            .setSleepTime(3000)
            .setUserAgent("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2.1 Safari/605.1.15")
            .addHeader("Accept-Language", "zh-TW,zh-Hant;q=0.9")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Encoding", "gzip, deflate, br");

    public static AtomicInteger limit = new AtomicInteger(0);

    @Override
    public void process(Page page) {
        Html html = page.getHtml();

        String[] idName = html.xpath("//a[@class='link_blue' and @style='font-size:14pt;font-weight:bold;']/text()").get().split(" +");
        String date = html.xpath("//nobr[@style='font-size:9pt;color:gray;']/text()").get();

        //非今日資料不處理
        if (!validDate(date)) {
            log.warn("{} 資料日期為 {} 今日未更新, {}", idName[1] + "(" + idName[0] + ")", date, LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
            //todo 警告入庫
            page.putField(Term.STOCK_ID.getFieldName(), idName[0]);
            page.putField(Term.STOCK_NAME.getFieldName(), idName[1]);
            page.putField(Term.DATE.getFieldName(), LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            page.putField(Term.TODAY_CLOSING_PRICE.getFieldName(), null);
            page.putField(Term.YESTERDAY_CLOSING_PRICE.getFieldName(), null);
            page.putField(Term.PRICE_GAP.getFieldName(), null);
            page.putField(Term.PRICE_GAP_PERCENT.getFieldName(), null);
            page.putField(Term.OPENING_PRICE.getFieldName(), null);
            page.putField(Term.HIGHEST_PRICE.getFieldName(), null);
            page.putField(Term.LOWEST_PRICE.getFieldName(), null);
            page.putField(Term.TODAY_TRADING_VOLUME_PIECE.getFieldName(), null);
            page.putField(Term.TODAY_TRADING_VOLUME_MONEY.getFieldName(), null);
            page.putField(Term.YESTERDAY_TRADING_VOLUME_PIECE.getFieldName(), null);
            page.putField(Term.YESTERDAY_TRADING_VOLUME_MONEY.getFieldName(), null);
        } else {
            List<Selectable> nodes = html.xpath("//table[@class='b1 p4_2 r10 box_shadow']//tr[@align='center' and @bgcolor='white']").nodes();
            Selectable priceRow = nodes.getFirst();
            Selectable todayTradingRow = nodes.get(1);
            Selectable yesterdayTradingRow = nodes.getLast();

            // main frame
            String todayClosingPrice = priceRow.xpath("//td[1]/text()").get();
            String yesterdayClosingPrice = priceRow.xpath("//td[2]/text()").get();
            String priceGap = priceRow.xpath("//td[3]/text()").get();
            String priceGapPercent = priceRow.xpath("//td[4]/text()").get();
            String openingPrice = priceRow.xpath("//td[6]/text()").get();
            String highestPrice = priceRow.xpath("//td[7]/text()").get();
            String lowestPrice = priceRow.xpath("//td[8]/text()").get();
            String todayTradingVolumePiece = todayTradingRow.xpath("//td[1]/text()").get();
            String todayTradingVolumeMoney = todayTradingRow.xpath("//td[2]/nobr/text()").get();
            String yesterdayTradingVolumePiece = yesterdayTradingRow.xpath("//td[1]/text()").get();
            String yesterdayTradingVolumeMoney = yesterdayTradingRow.xpath("//td[2]/nobr/text()").get();

            //組裝交付給pipeline資料
            page.putField(Term.STOCK_ID.getFieldName(), idName[0]);
            page.putField(Term.STOCK_NAME.getFieldName(), idName[1]);
            page.putField(Term.DATE.getFieldName(), LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            page.putField(Term.TODAY_CLOSING_PRICE.getFieldName(), todayClosingPrice);
            page.putField(Term.YESTERDAY_CLOSING_PRICE.getFieldName(), yesterdayClosingPrice);
            page.putField(Term.PRICE_GAP.getFieldName(), priceGap);
            page.putField(Term.PRICE_GAP_PERCENT.getFieldName(), priceGapPercent);
            page.putField(Term.OPENING_PRICE.getFieldName(), openingPrice);
            page.putField(Term.HIGHEST_PRICE.getFieldName(), highestPrice);
            page.putField(Term.LOWEST_PRICE.getFieldName(), lowestPrice);
            page.putField(Term.TODAY_TRADING_VOLUME_PIECE.getFieldName(), todayTradingVolumePiece);
            page.putField(Term.TODAY_TRADING_VOLUME_MONEY.getFieldName(), todayTradingVolumeMoney);
            page.putField(Term.YESTERDAY_TRADING_VOLUME_PIECE.getFieldName(), yesterdayTradingVolumePiece);
            page.putField(Term.YESTERDAY_TRADING_VOLUME_MONEY.getFieldName(), yesterdayTradingVolumeMoney);
        }

        List<String> all = html.links().regex("^https:\\/\\/goodinfo\\.tw\\/tw\\/StockDetail\\.asp\\?STOCK_ID=.+$").all();
        List<String> newUrls = new ArrayList<>();
        for (String url : all) {
            if (url.contains("&INITIALIZED=T")) {
                newUrls.add(url.replace("&INITIALIZED=T", ""));
            } else {
                newUrls.add(url);
            }
        }
        page.addTargetRequests(newUrls);

    }

    @Override
    public Site getSite() {
        return site;
    }

    private boolean validDate(String content) {
        try {
            LocalDate now = LocalDate.now();
            String date = content.split(":")[1].trim();
            String[] monthDate = date.split("/");

            return now.getMonth().getValue() == Integer.parseInt(monthDate[0]) && now.getDayOfMonth() == Integer.parseInt(monthDate[1]);
        } catch (Exception e) {
            log.error("GoodInfoScraper valid date go wrong, content: {}", content);
            return false;
        }
    }

    public static void main(String[] args) {
        List<String> urls = List.of("aaaa&INITIALIZED=T", "bbbb&INITIALIZED=T", "cccc&INITIALIZED=T");

        for (String url : urls) {
            url = url.replace("&INITIALIZED=T", "");
        }

        System.out.println(urls);
    }
}
