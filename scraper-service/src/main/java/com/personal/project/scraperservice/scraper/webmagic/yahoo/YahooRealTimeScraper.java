package com.personal.project.scraperservice.scraper.webmagic.yahoo;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class YahooRealTimeScraper implements PageProcessor {

	private final Site site = Site.me()
			.setDefaultCharset("utf-8")
			.setCycleRetryTimes(0)
			.setSleepTime(3000)
			.setUserAgent("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2.1 Safari/605.1.15")
			.addHeader("Accept-Language", "zh-TW,zh-Hant;q=0.9")
			.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
			.addHeader("Accept-Encoding", "gzip, deflate, br");

	private final List<String> urls;

	private final List<ScraperErrorMessageDO> errors = Collections.synchronizedList(new ArrayList<>());

	private final Long date;

	public YahooRealTimeScraper(List<String> urls, Long date) {
		this.urls = urls;
		this.date = date;
	}

	@Override
	public void process(Page page) {
		page.addTargetRequests(urls);
		Html html = page.getHtml();
		List<Selectable> rows = html.xpath("//*[@id='main-1-ClassQuotesTable-Proxy']//div[@class='table-body-wrapper']/ul/li").nodes();
		try {
			List<DailyStockInfoDto> results = new ArrayList<>();

			for (Selectable row : rows) {
				DailyStockInfoDto dto = new DailyStockInfoDto();
				//id
				String stockId = row.xpath("//li//span[@class='Fz(14px) C(#979ba7) Ell']/text()").get().trim().replace(".TWO", "").replace(".TW", "");
				dto.setStockId(stockId);
				if (stockId.trim().length() > 4 || !StrUtil.isNumeric(stockId.trim())) {
					continue;
				}

				//name
				try {
					String stockName = row.xpath("//li//div[@class='Lh(20px) Fw(600) Fz(16px) Ell']/text()").get().trim();
					dto.setStockName(stockName);
				} catch (Exception ignored) {}

				dto.setDate(date);

				//成交
				List<Selectable> pNodes = row.xpath("//li//div[@class='Fxg(1) Fxs(1) Fxb(0%) Ta(end) Mend($m-table-cell-space) Mend(0):lc Miw(68px)']").nodes();
				try {
					String nowP = pNodes.getFirst().xpath("//div//span/text()").get().trim();
					dto.setTodayClosingPrice(new BigDecimal(nowP));
				} catch (Exception ignored) {
					continue;
				}

				//昨收
				try {
					String yesterdayC = pNodes.get(2).xpath("//div/span/text()").get().trim();
					dto.setYesterdayClosingPrice(new BigDecimal(yesterdayC));
				} catch (Exception ignored) {
					continue;
				}

				//漲跌
				List<Selectable> udNode = row.xpath("//li//div[@class='Fxg(1) Fxs(1) Fxb(0%) Ta(end) Mend($m-table-cell-space) Mend(0):lc Miw(74px)']").nodes();
				String pGap = udNode.getFirst().xpath("//div/span/text()").get().trim();
				if (dto.getTodayClosingPrice().compareTo(dto.getYesterdayClosingPrice()) < 0) {
					dto.setPriceGap(BigDecimal.ZERO.subtract(new BigDecimal(pGap)));
				} else {
					dto.setPriceGap(new BigDecimal(pGap));
				}

				//漲跌幅
				String pGapP = udNode.getLast().xpath("//div/span/text()").get().trim().replace("%", "");
				dto.setPriceGapPercent(new BigDecimal(pGapP));

				//開
				try {
					String op = pNodes.get(1).xpath("//div/span/text()").get().trim();
					dto.setOpeningPrice(new BigDecimal(op));
				} catch (Exception ignored) {
					dto.setOpeningPrice(dto.getYesterdayClosingPrice());
				}

				//高
				try {
					String high = pNodes.get(3).xpath("//div/span/text()").get().trim();
					dto.setHighestPrice(new BigDecimal(high));
				} catch (Exception ignored) {
					dto.setHighestPrice(dto.getTodayClosingPrice());
				}

				//低
				try {
					String low = pNodes.get(4).xpath("//div/span/text()").get().trim();
					dto.setLowestPrice(new BigDecimal(low));
				} catch (Exception ignored) {
					dto.setLowestPrice(dto.getTodayClosingPrice());
				}

				//成量(張)
				String vol = row.xpath("//li//div[@class='Fxg(1) Fxs(1) Fxb(0%) Miw($w-table-cell-min-width) Ta(end) Mend($m-table-cell-space) Mend(0):lc']/span/text()").get().trim().replace(",", "");
				dto.setTodayTradingVolumePiece(Long.parseLong(vol));

				results.add(dto);
			}

			page.putField("dtos", results);
		} catch (Exception e) {
			log.error("Error when downloading {}", page.getUrl(), e);
			page.setSkip(true);
		}

	}

	@Override
	public Site getSite() {
		return site;
	}

	public String getFirstUrl() {
		return urls.getFirst();
	}
}
