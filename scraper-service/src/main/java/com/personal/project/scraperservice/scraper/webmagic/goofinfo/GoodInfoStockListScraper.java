package com.personal.project.scraperservice.scraper.webmagic.goofinfo;

import cn.hutool.core.util.StrUtil;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoodInfoStockListScraper implements PageProcessor {

    private Site site = Site.me()
            .setDefaultCharset("utf-8")
            .setCycleRetryTimes(0)
            .setSleepTime(5000)
            .setUserAgent("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2.1 Safari/605.1.15")
            .addHeader("Accept-Language", "zh-TW,zh-Hant;q=0.9")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Encoding", "gzip, deflate, br");

    @Override
    public void process(Page page) {
        Html html = page.getHtml();
        Map<String, Map<String, String>> marketToObj = new HashMap<>();
        List<Selectable> trs = html.xpath("//table[@id='tblStockList']/tbody/tr").nodes();
        for (Selectable tr : trs) {
            if (!StrUtil.isEmpty(tr.xpath("//tr/@id").get())) {
                String stockId = tr.xpath("//tr/td[1]/nobr/a/text()").get();
                if (!StrUtil.isNumeric(stockId) && stockId.length() != 4) {
                    continue;
                }
                String stockName = tr.xpath("//tr/td[2]/nobr/a/text()").get();
                String market = tr.xpath("//tr/td[3]/nobr/text()").get();
                if (marketToObj.get(market) == null) {
                    marketToObj.put(market, new HashMap<>() {{
                        put(stockId, stockName);
                    }});
                } else {
                    marketToObj.get(market).put(stockId, stockName);
                }
            }
        }

        page.putField("marketToObj", marketToObj);
    }

    @Override
    public Site getSite() {
        return site;
    }
}
