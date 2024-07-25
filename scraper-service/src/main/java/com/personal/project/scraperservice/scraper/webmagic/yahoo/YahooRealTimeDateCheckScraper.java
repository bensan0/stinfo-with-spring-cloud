package com.personal.project.scraperservice.scraper.webmagic.yahoo;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;

public class YahooRealTimeDateCheckScraper  implements PageProcessor {

	private final Site site = Site.me()
			.setDefaultCharset("utf-8")
			.setCycleRetryTimes(0)
			.setSleepTime(3000)
			.setUserAgent("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2.1 Safari/605.1.15")
			.addHeader("Accept-Language", "zh-TW,zh-Hant;q=0.9")
			.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
			.addHeader("Accept-Encoding", "gzip, deflate, br");

	@Override
	public void process(Page page) {
		Html html = page.getHtml();
		String[] yMdSplit = html.xpath("//time/span[2]/text()").get().split("/");
		Long pageDate = Long.parseLong(yMdSplit[0] + yMdSplit[1] + yMdSplit[2]);
		page.putField("date", pageDate);
	}

	@Override
	public Site getSite() {
		return site;
	}
}
