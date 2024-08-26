package com.personal.project.scraperservice.scraper.webmagic.yahoo;

import com.personal.project.scraperservice.model.dto.DailyIndexInfoDTO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class YahooRealTimeIndexScraper implements PageProcessor {

	private final Site site = Site.me()
			.setDefaultCharset("utf-8")
			.setCycleRetryTimes(0)
			.setSleepTime(3000)
			.setUserAgent("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2.1 Safari/605.1.15")
			.addHeader("Accept-Language", "zh-TW,zh-Hant;q=0.9")
			.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
			.addHeader("Accept-Encoding", "gzip, deflate, br");

	private final BlockingQueue<String> urls;

	private final Long date;

	@Getter
	private boolean isToday = true;

	public YahooRealTimeIndexScraper(BlockingQueue<String> urls, Long date) {
		this.urls = urls;
		this.date = date;
	}

	@Override
	public void process(Page page) {
		Html html = page.getHtml();
		String date = html.xpath("//time/span[2]/text()").get().trim().split(" +")[0].replace("/", "");
		if (!this.date.toString().equals(date)) {
			page.setSkip(true);
			isToday = false;
			return;
		}

		page.addTargetRequest(urls.poll());

		String indexName = html.xpath("//h1[@class='C($c-link-text) Fw(b) Fz(24px) Mend(8px)']/text()").get();

		List<Selectable> rows = html.xpath("//*[@id='qsp-overview-realtime-info']//div[@class='Fx(n) W(316px) Bxz(bb) Pstart(16px) Pt(12px)']/div[@class='Pos(r)']/ul/li").nodes();

		try {
			DailyIndexInfoDTO result = new DailyIndexInfoDTO();
			result.setIndexName(indexName);
			result.setDate(this.date);

			for (Selectable row : rows) {
				String rowName = row.xpath("//li/span[1]/text()").get();

				switch (rowName) {
					case "成交" ->
							result.setTodayClosing(new BigDecimal(row.xpath("//li/span[2]/text()").get().replace(",", "")));
					case "開盤" ->
							result.setOpening(new BigDecimal(row.xpath("//li/span[2]/text()").get().replace(",", "")));
					case "最高" ->
							result.setHighest(new BigDecimal(row.xpath("//li/span[2]/text()").get().replace(",", "")));
					case "最低" ->
							result.setLowest(new BigDecimal(row.xpath("//li/span[2]/text()").get().replace(",", "")));
					case "昨收" ->
							result.setYesterdayClosing(new BigDecimal(row.xpath("//li/span[2]/text()").get().replace(",", "")));
					case "漲跌幅" ->
							result.setGapPercent(new BigDecimal(row.xpath("//li/span[2]/text()").get().replace("%", "")));
					case "漲跌" -> result.setGap(new BigDecimal(row.xpath("//li/span[2]/text()").get()));
					case "總量" ->
							result.setTodayTradingVolume(Long.parseLong(row.xpath("//li/span[2]/text()").get().replace(",", "")));
					case "成交金額(億)" ->
							result.setTodayTradingAmount(new BigDecimal(row.xpath("//li/span[2]/text()").get().replace(",", "")).multiply(BigDecimal.valueOf(100000000)));
				}
			}

			if (result.getTodayClosing().compareTo(result.getYesterdayClosing()) < 0) {
				result.setGap(BigDecimal.ZERO.subtract(result.getGap()));
				result.setGapPercent(BigDecimal.ZERO.subtract(result.getGapPercent()));
			}

			page.putField("dto", result);
		} catch (Exception e) {
			log.error("Error when downloading {}", page.getUrl(), e);
			page.setSkip(true);
		}
	}

	@Override
	public Site getSite() {
		return site;
	}
}
