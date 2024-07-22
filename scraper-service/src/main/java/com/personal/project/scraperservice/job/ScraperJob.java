package com.personal.project.scraperservice.job;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.scraperservice.constant.CacheKeys;
import com.personal.project.scraperservice.model.dto.DailyStockInfoDto;
import com.personal.project.scraperservice.model.entity.ScraperErrorMessageDO;
import com.personal.project.scraperservice.remote.RemoteReportService;
import com.personal.project.scraperservice.remote.RemoteStockService;
import com.personal.project.scraperservice.scraper.webmagic.SeleniumDownloader;
import com.personal.project.scraperservice.scraper.webmagic.tpex.TPEXInitPipeline;
import com.personal.project.scraperservice.scraper.webmagic.tpex.TPEXInitScraper;
import com.personal.project.scraperservice.scraper.webmagic.tpex.TPEXRoutinePipeline;
import com.personal.project.scraperservice.scraper.webmagic.tpex.TPEXRoutineScraper;
import com.personal.project.scraperservice.scraper.webmagic.twse.TWSEInitPipeline;
import com.personal.project.scraperservice.scraper.webmagic.twse.TWSEInitScraper;
import com.personal.project.scraperservice.scraper.webmagic.twse.TWSERoutinePipeline;
import com.personal.project.scraperservice.scraper.webmagic.twse.TWSERoutineScraper;
import com.personal.project.scraperservice.scraper.webmagic.yahoo.YahooRealTimePipeline;
import com.personal.project.scraperservice.scraper.webmagic.yahoo.YahooRealTimeScraper;
import com.personal.project.scraperservice.service.CacheService;
import com.personal.project.scraperservice.service.ErrorMessageService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassPathUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.redisson.api.RLock;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Spider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Component
@Slf4j
public class ScraperJob {

	private final CacheService cacheService;

	private final RemoteStockService remoteStockService;

	private final RemoteReportService remoteReportService;

	private final ErrorMessageService errorMessageService;

	private static final String classpath = ClassPathUtils.class.getClassLoader().getResource("").getFile();

	private static final String driverPath = "driver/chrome-driver-mac-arm64/chromedriver";

	public ScraperJob(RemoteStockService remoteStockService, RemoteReportService remoteReportService, ErrorMessageService errorMessageService, CacheService cacheService) {
		this.remoteStockService = remoteStockService;
		this.errorMessageService = errorMessageService;
		this.remoteReportService = remoteReportService;
		this.cacheService = cacheService;
	}

	//半年線,年線都先暫不計算(routine任務關於這部分計算都先去除)
	//todo 另外分一個任務可帶參數去獲取更久以前的資料(以後計算年線半年線用)
	@XxlJob("twse-tpex-system-init-scrape")
	public void twseAndTPEXInitHandle() {
		//檢查是否已經有資料存在
		InnerResponse<Boolean> checkRes = remoteStockService.checkInit(null);
		if (checkRes.getData()) {
			log.error("已存在資料, 資料初始化任務 twse-tpex-system-init-scrape 不執行");
			return;
		}

		RLock lock = cacheService.getLock(CacheKeys.INIT_DATA_CACHE.getLock());
		try {
			if (lock.tryLock(10, 3600, TimeUnit.SECONDS)) {
				int offMonths = 6; //現在往前推幾個月～本月資料
				LocalDate now = LocalDate.now();
				log.info("資料初始化任務 twse-tpex-system-init-scrape 開始執行");
				TimeInterval timer = DateUtil.timer();

				//日期清單
				List<LocalDate> dates = new ArrayList<>();
				for (LocalDate i = now; !i.isBefore(i.minusMonths(offMonths)); i = i.minusDays(1)) {
					if (i.getDayOfWeek() == DayOfWeek.SATURDAY || i.getDayOfWeek() == DayOfWeek.SUNDAY) {
						continue;
					}

					dates.add(i);
				}

				try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
					//獲取上市股票
					Future<?> listedFuture = executorService.submit(() -> {
						log.info("資料初始化任務 twse-tpex-system-init-scrape, 上市日期：{}-{}   爬蟲開始", dates.getFirst(), dates.getLast());

						//產出下載地址
						List<String> twseUrls = generateTWSEUrls(dates);

						//爬蟲開始
						TWSEInitScraper scraper = new TWSEInitScraper();
						scraper.setUrls(twseUrls);
						TWSEInitPipeline pipeline = new TWSEInitPipeline();

						Pair<Map<String, List<DailyStockInfoDto>>, List<ScraperErrorMessageDO>> rawData = scrapeTWSEInitDailyStockInfo(scraper, pipeline);
						Map<String, List<DailyStockInfoDto>> data = rawData.getLeft();
						List<ScraperErrorMessageDO> errors = rawData.getRight();

						data.forEach((k, v) -> v.sort(Comparator.comparingLong(DailyStockInfoDto::getDate).reversed()));

						//資料清洗
						cleanupTWSEData(data);

						//資料補全
						completeTWSEData(data);

						//填充
						List<DailyStockInfoDto> finalResults = fillData(data);

						//資料入庫
						InnerResponse<ObjectUtils.Null> response = remoteStockService.initSaveAll(finalResults, null);
						log.info("資料初始化任務 twse-tpex-system-init-scrape 上市個股 入庫結束, {}", JSON.toJSON(response));

						//錯誤入庫
						if (errors != null && !errors.isEmpty()) {
							boolean saved = errorMessageService.saveBatch(errors);
							log.info("資料初始化任務 twse-tpex-system-init-scrape 上市 錯誤入庫結束, {}", saved);
						} else {
							log.info("資料初始化任務 twse-tpex-system-init-scrape 上市 無錯誤");
						}

					});


					Future<?> otcFuture = executorService.submit(() -> {
						log.info("資料初始化任務 twse-tpex-system-init-scrape, 上櫃爬蟲開始");

						//產出下載地址
						List<String> tpexUrls = generateTPEXUrls(dates);

						TPEXInitScraper tpexInitScraper = new TPEXInitScraper(tpexUrls);
						TPEXInitPipeline tpexInitPipeline = new TPEXInitPipeline();

						//爬蟲開始
						Pair<Map<String, List<DailyStockInfoDto>>, List<ScraperErrorMessageDO>> rawData = scrapeTPEXInitDailyStockInfo(tpexInitScraper, tpexInitPipeline);

						Map<String, List<DailyStockInfoDto>> data = rawData.getLeft();
						List<ScraperErrorMessageDO> errors = rawData.getRight();

						data.forEach((k, v) -> v.sort(Comparator.comparingLong(DailyStockInfoDto::getDate).reversed()));

						//清洗
						cleanupTPEXData(data);

						//補全
						completeTPEXData(data);

						//填充
						List<DailyStockInfoDto> finalResults = fillData(data);

						//資料入庫
						InnerResponse<ObjectUtils.Null> response = remoteStockService.initSaveAll(finalResults, null);
						log.info("資料初始化任務 twse-tpex-system-init-scrape 上櫃個股 入庫結束, {}", JSON.toJSON(response));

						//錯誤入庫
						if (errors != null && !errors.isEmpty()) {
							boolean saved = errorMessageService.saveBatch(errors);
							log.info("資料初始化任務 twse-tpex-system-init-scrape 上櫃 錯誤入庫結束, {}", saved);
						} else {
							log.info("資料初始化任務 twse-tpex-system-init-scrape 上櫃個股, 無錯誤");
						}
					});

					listedFuture.get();
					otcFuture.get();

				} catch (InterruptedException | ExecutionException e) {
					log.error("oh my god", e);
				}

				log.error("資料初始化任務 twse-tpex-system-init-scrape 爬蟲部分結束, 花費 {} 毫秒", timer.intervalRestart());

				//call remote report to cal yesterday metric and detail
				InnerResponse<ObjectUtils.Null> response1 = remoteReportService.initYesterdayReport(null);
				log.info("資料初始化任務 twse-tpex-system-init-scrape 初始化上一個交易日的指標報告/詳細報告花費 {} 毫秒, 結果 {}", timer.intervalRestart(), JSON.toJSON(response1));

				//call remote report to cal newest trading day metric and detail
				InnerResponse<ObjectUtils.Null> response2 = remoteReportService.initTodayReport(null);
				log.info("資料初始化任務 twse-tpex-system-init-scrape 初始化最新交易日的指標報告/詳細報告花費 {} 毫秒, 結果 {}", timer.intervalRestart(), JSON.toJSON(response2));
			}
		} catch (InterruptedException e) {
			log.error("lock error", e);
		} finally {
			if (lock.isLocked()) {
				lock.unlock();
			}
		}

		log.info("資料初始化任務 twse-tpex-system-init-scrape 結束");
	}

	/**
	 * 產出TWSE個股日成交資訊地址
	 *
	 * @return
	 */
	private List<String> generateTWSEUrls(List<LocalDate> dates) {
		List<String> twseUrls = new ArrayList<>();
		for (LocalDate date : dates) {
			twseUrls.add(StrUtil.format("https://www.twse.com.tw/rwd/zh/afterTrading/MI_INDEX?date={}&type=ALL&response=html", date.format(DatePattern.PURE_DATE_FORMATTER)));
		}

		return twseUrls;
	}

	/**
	 * 爬TWSE(上市)個股日成交資訊
	 *
	 * @param twseInitScraper
	 * @param twseInitPipeline
	 * @return
	 */
	private Pair<Map<String, List<DailyStockInfoDto>>, List<ScraperErrorMessageDO>> scrapeTWSEInitDailyStockInfo(TWSEInitScraper twseInitScraper, TWSEInitPipeline twseInitPipeline) {
		Spider.create(twseInitScraper)
				.addUrl(twseInitScraper.getFirstUrl())
				.addPipeline(twseInitPipeline)
				.thread(2)
				.run();

		Map<String, List<DailyStockInfoDto>> twseResult = twseInitPipeline.getResult();
		List<ScraperErrorMessageDO> pipelineErrors = twseInitScraper.getErrors();//pipe line 錯誤

		return Pair.of(twseResult, pipelineErrors);
	}

	/**
	 * 清洗TWSE資料(補全高開低收)
	 * TWSE資料於開高低收有可能為--, 此處替換為前一日收盤
	 *
	 * @param sortedData
	 */
	private void cleanupTWSEData(Map<String, List<DailyStockInfoDto>> sortedData) {
		sortedData.forEach((k, v) -> {
			for (int i = 0; i < v.size(); i++) {
				DailyStockInfoDto dataLostDTO = v.get(i);
				if (dataLostDTO.getTodayClosingPrice() == null) {
					List<DailyStockInfoDto> subList;
					BigDecimal lastEffectivePrice = null;
					if (i == 0) {
						subList = v.subList(1, v.size());
						lastEffectivePrice = subList.stream()
								.filter(dto -> dto.getTodayClosingPrice() != null)
								.findFirst()
								.map(DailyStockInfoDto::getTodayClosingPrice)
								.orElse(lastEffectivePrice);
						log.error("無效日期最新, 上市：{}, 日期：{}, 有效：{}", dataLostDTO.getStockId(), dataLostDTO.getDate(), lastEffectivePrice);
					} else if (i == v.size() - 1) {
						subList = v.subList(0, i).reversed();
						lastEffectivePrice = subList.stream()
								.filter(dto -> dto.getTodayClosingPrice() != null)
								.findFirst()
								.map(dto -> dto.getTodayClosingPrice().subtract(dto.getPriceGap()))
								.orElse(lastEffectivePrice);
						log.error("無效日期最舊, 上市：{}, 日期：{}, 有效：{}", dataLostDTO.getStockId(), dataLostDTO.getDate(), lastEffectivePrice);
					} else {
						subList = v.subList(i, v.size());
						lastEffectivePrice = subList.stream()
								.filter(dto -> dto.getTodayClosingPrice() != null)
								.findFirst()
								.map(DailyStockInfoDto::getTodayClosingPrice)
								.orElse(lastEffectivePrice);
						if (lastEffectivePrice == null) {
							subList = v.subList(0, i).reversed();
							lastEffectivePrice = subList.stream()
									.filter(dto -> dto.getTodayClosingPrice() != null)
									.findFirst()
									.map(dto -> dto.getTodayClosingPrice().subtract(dto.getPriceGap()))
									.orElse(lastEffectivePrice);
						}
						log.error("無效日期中間, 上市：{}, 日期：{}, 有效：{}", dataLostDTO.getStockId(), dataLostDTO.getDate(), lastEffectivePrice);
					}
					if (lastEffectivePrice == null) {
						log.error("上市沒救, 往前後找都ＧＧ, {}, {}", k, dataLostDTO.getDate());
					}
					dataLostDTO.setTodayClosingPrice(lastEffectivePrice);

					if (dataLostDTO.getHighestPrice() == null) {
						dataLostDTO.setHighestPrice(lastEffectivePrice);
					}
					if (dataLostDTO.getOpeningPrice() == null) {
						dataLostDTO.setOpeningPrice(lastEffectivePrice);
					}
					if (dataLostDTO.getLowestPrice() == null) {
						dataLostDTO.setLowestPrice(lastEffectivePrice);
					}
				}
			}
		});
	}

	/**
	 * 補全TWSE資料(昨額 昨收 昨量 差額 漲跌幅)
	 *
	 * @param sortedData
	 */
	private void completeTWSEData(Map<String, List<DailyStockInfoDto>> sortedData) {
		sortedData.forEach((k, v) -> {
			DailyStockInfoDto tempDTO = null;
			for (int i = 0; i < v.size(); i++) {
				DailyStockInfoDto thisRoundDTO = v.get(i);
				//tempDTO填上缺失的昨日資訊
				if (tempDTO != null) {
					tempDTO.setYesterdayClosingPrice(thisRoundDTO.getTodayClosingPrice());
					tempDTO.setYesterdayTradingVolumePiece(thisRoundDTO.getTodayTradingVolumePiece());
					tempDTO.setYesterdayTradingVolumeMoney(thisRoundDTO.getTodayTradingVolumeMoney());
					tempDTO.setPriceGap(tempDTO.getTodayClosingPrice().subtract(tempDTO.getYesterdayClosingPrice()));
					tempDTO.setPriceGapPercent(tempDTO.getPriceGap().divide(tempDTO.getYesterdayClosingPrice(), 4, RoundingMode.FLOOR).multiply(BigDecimal.valueOf(100)));
				}
				tempDTO = thisRoundDTO;

				if (i == v.size() - 1) {
					thisRoundDTO.setPriceGapPercent(null);
					thisRoundDTO.setPriceGap(null);
					thisRoundDTO.setYesterdayClosingPrice(null);
				}
			}
		});
	}

	/**
	 * 填充平日但非交易日的資料
	 */
	private List<DailyStockInfoDto> fillData(Map<String, List<DailyStockInfoDto>> sortedData) {
		List<DailyStockInfoDto> finalResults = new ArrayList<>();
		sortedData.forEach((k, v) -> {
			DailyStockInfoDto tempDTO = null;
			for (int i = 0; i < v.size(); i++) {
				DailyStockInfoDto thisRoundDTO = v.get(i);
				if (tempDTO != null) {
					LocalDate tempDTODate = LocalDate.parse(tempDTO.getDate().toString(), DatePattern.PURE_DATE_FORMATTER);
					LocalDate thisDate = LocalDate.parse(thisRoundDTO.getDate().toString(), DatePattern.PURE_DATE_FORMATTER);
					if (!thisDate.isEqual(tempDTODate.minusDays(1)) &&
							thisDate.getDayOfWeek() != DayOfWeek.FRIDAY) {
						LocalDate flagDate = tempDTODate.minusDays(1);
						while (!flagDate.isEqual(thisDate)) {
							if (flagDate.getDayOfWeek() != DayOfWeek.SATURDAY && flagDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
								DailyStockInfoDto fillDTO = new DailyStockInfoDto();
								fillDTO.setStockId(k);
								fillDTO.setStockName(thisRoundDTO.getStockName());
								fillDTO.setMarket(thisRoundDTO.getMarket());
								fillDTO.setDate(Long.valueOf(flagDate.format(DatePattern.PURE_DATE_FORMATTER)));
								finalResults.add(fillDTO);
							}
							flagDate = flagDate.minusDays(1);
						}
					}
				}
				tempDTO = thisRoundDTO;
			}
			finalResults.addAll(v);
		});

		return finalResults;
	}

	/**
	 * 爬TPEX(櫃買)個股日成交資訊
	 *
	 * @param tpexInitScraper
	 * @param tpexInitPipeline
	 * @return
	 */
	private Pair<Map<String, List<DailyStockInfoDto>>, List<ScraperErrorMessageDO>> scrapeTPEXInitDailyStockInfo(TPEXInitScraper tpexInitScraper, TPEXInitPipeline tpexInitPipeline) {
		Spider.create(tpexInitScraper)
				.addUrl(tpexInitScraper.getUrlsFirst())
				.addPipeline(tpexInitPipeline)
				.thread(2)
				.run();

		Map<String, List<DailyStockInfoDto>> tpexResult = tpexInitPipeline.getResult();
		List<ScraperErrorMessageDO> errors = tpexInitScraper.getErrors();

		return Pair.of(tpexResult, errors);
	}

	/**
	 * 產出TPEX個股日成交資訊地址
	 *
	 * @return
	 */
	private List<String> generateTPEXUrls(List<LocalDate> dates) {
		List<String> tpexUrls = new ArrayList<>();

		for (LocalDate date : dates) {
			tpexUrls.add(
					StrUtil.format(
							"https://www.tpex.org.tw/web/stock/aftertrading/otc_quotes_no1430/stk_wn1430_result.php?l=zh-tw&o=htm&d={}/{}/{}&se=AL&s=0,asc,0",
							date.getYear() - 1911,
							date.getMonth().getValue() < 10 ? "0" + date.getMonth().getValue() : date.getMonth().getValue(),
							date.getDayOfMonth() < 10 ? "0" + date.getDayOfMonth() : date.getDayOfMonth()
					)
			);
		}

		return tpexUrls;
	}

	/**
	 * 清洗TPEX資料(補全高開低收)
	 * TPEX資料於開高低收有可能為--, 此處替換為前一日收盤
	 *
	 * @param sortedData
	 */
	private void cleanupTPEXData(Map<String, List<DailyStockInfoDto>> sortedData) {
		sortedData.forEach((k, v) -> {
			for (int i = 0; i < v.size(); i++) {
				DailyStockInfoDto dataLostDTO = v.get(i);
				if (dataLostDTO.getTodayClosingPrice() == null) {
					List<DailyStockInfoDto> subList;
					BigDecimal lastEffectivePrice = null;
					if (i == 0) {
						subList = v.subList(1, v.size());
						lastEffectivePrice = subList.stream()
								.filter(dto -> dto.getTodayClosingPrice() != null)
								.findFirst()
								.map(DailyStockInfoDto::getTodayClosingPrice)
								.orElse(lastEffectivePrice);
						log.error("無效日期最新, 上櫃：{}, 日期：{}, 有效：{}", dataLostDTO.getStockId(), dataLostDTO.getDate(), lastEffectivePrice);
					} else if (i == v.size() - 1) {
						subList = v.subList(0, i).reversed();
						lastEffectivePrice = subList.stream()
								.filter(dto -> dto.getTodayClosingPrice() != null)
								.findFirst()
								.map(dto -> dto.getTodayClosingPrice().subtract(dto.getPriceGap()))
								.orElse(lastEffectivePrice);
						log.error("無效日期最舊, 上櫃：{}, 日期：{}, 有效：{}", dataLostDTO.getStockId(), dataLostDTO.getDate(), lastEffectivePrice);
					} else {
						subList = v.subList(i, v.size());
						lastEffectivePrice = subList.stream()
								.filter(dto -> dto.getTodayClosingPrice() != null)
								.findFirst()
								.map(DailyStockInfoDto::getTodayClosingPrice)
								.orElse(lastEffectivePrice);
						if (lastEffectivePrice == null) {
							subList = v.subList(0, i).reversed();
							lastEffectivePrice = subList.stream()
									.filter(dto -> dto.getTodayClosingPrice() != null)
									.findFirst()
									.map(dto -> dto.getTodayClosingPrice().subtract(dto.getPriceGap()))
									.orElse(lastEffectivePrice);
						}
						log.error("無效日期中間, 上櫃：{}, 日期：{}, 有效：{}", dataLostDTO.getStockId(), dataLostDTO.getDate(), lastEffectivePrice);
					}
					if (lastEffectivePrice == null) {
						log.error("上櫃沒救, 往前後找都ＧＧ, {}, {}", k, dataLostDTO.getDate());
					}
					dataLostDTO.setTodayClosingPrice(lastEffectivePrice);

					if (dataLostDTO.getHighestPrice() == null) {
						dataLostDTO.setHighestPrice(lastEffectivePrice);
					}
					if (dataLostDTO.getOpeningPrice() == null) {
						dataLostDTO.setOpeningPrice(lastEffectivePrice);
					}
					if (dataLostDTO.getLowestPrice() == null) {
						dataLostDTO.setLowestPrice(lastEffectivePrice);
					}
				}
			}
		});
	}

	/**
	 * 補全TPEX資料(差額, 昨收, 漲跌幅, 昨額 昨量)
	 *
	 * @param sortedData
	 */
	private void completeTPEXData(Map<String, List<DailyStockInfoDto>> sortedData) {
		sortedData.forEach((k, v) -> {
			DailyStockInfoDto tempDTO = null;
			for (int i = 0; i < v.size(); i++) {
				DailyStockInfoDto thisRoundDTO = v.get(i);
				//tempDTO填上缺失的昨日資訊
				if (tempDTO != null) {
					tempDTO.setYesterdayClosingPrice(thisRoundDTO.getTodayClosingPrice());
					tempDTO.setYesterdayTradingVolumePiece(thisRoundDTO.getTodayTradingVolumePiece());
					tempDTO.setYesterdayTradingVolumeMoney(thisRoundDTO.getTodayTradingVolumeMoney());
					tempDTO.setPriceGap(tempDTO.getTodayClosingPrice().subtract(tempDTO.getYesterdayClosingPrice()));
					tempDTO.setPriceGapPercent(tempDTO.getPriceGap().divide(tempDTO.getYesterdayClosingPrice(), 4, RoundingMode.FLOOR).multiply(BigDecimal.valueOf(100)));
				}
				tempDTO = thisRoundDTO;

				if (i == v.size() - 1) {
					thisRoundDTO.setPriceGapPercent(null);
					thisRoundDTO.setPriceGap(null);
					thisRoundDTO.setYesterdayClosingPrice(null);
				}
			}
		});
	}


	@XxlJob("twse-tpex-routine-scrape")
	public void twseJobHandle() {
		RLock lock = cacheService.getLock(CacheKeys.SCRAPE_INFO_CACHE.getLock());
		try {
			if (lock.tryLock(5, 30, TimeUnit.SECONDS)) {
				log.info("爬蟲任務 twse-tpex-routine-scrape 開始執行");
				LocalDate now = LocalDate.now();
				String nowStr = now.format(DatePattern.PURE_DATE_FORMATTER);

				List<DailyStockInfoDto> results = new ArrayList<>();
				List<ScraperErrorMessageDO> errors = new ArrayList<>();

				try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {

					Future<Pair<List<DailyStockInfoDto>, List<ScraperErrorMessageDO>>> twseResults = executorService.submit(() -> {
						log.info("twse下載執行");
						TWSERoutineScraper scraper = new TWSERoutineScraper(Long.parseLong(nowStr));
						TWSERoutinePipeline pipeline = new TWSERoutinePipeline();
						Spider.create(scraper)
								.addUrl("https://www.twse.com.tw/rwd/zh/afterTrading/MI_INDEX?date=" + nowStr + "&type=ALL&response=html")
								.addPipeline(pipeline)
								.thread(1)
								//啟動爬蟲
								.run();

						return Pair.of(pipeline.getResults(), scraper.getErrors());
					});

					Future<Pair<List<DailyStockInfoDto>, List<ScraperErrorMessageDO>>> tpexResults = executorService.submit(() -> {
						log.info("tpex下載執行");
						TPEXRoutineScraper scraper = new TPEXRoutineScraper(Long.parseLong(nowStr));
						TPEXRoutinePipeline pipeline = new TPEXRoutinePipeline();
						Spider.create(scraper)
								.addUrl("https://www.tpex.org.tw/web/stock/aftertrading/otc_quotes_no1430/stk_wn1430_result.php?l=zh-tw&o=htm&d=" +
										StrUtil.format("{}/{}/{}",
												now.getYear() - 1911,
												now.getMonth().getValue() < 10 ? "0" + now.getMonth().getValue() : now.getMonth().getValue(),
												now.getDayOfMonth() < 10 ? "0" + now.getDayOfMonth() : now.getDayOfMonth()) +
										"&se=AL&s=0,asc,0")
								.addPipeline(pipeline)
								.thread(1)
								.run();

						return Pair.of(pipeline.getResults(), scraper.getErrors());
					});
					Pair<List<DailyStockInfoDto>, List<ScraperErrorMessageDO>> twsePair = twseResults.get();
					Pair<List<DailyStockInfoDto>, List<ScraperErrorMessageDO>> tpexPair = tpexResults.get();
					results.addAll(twsePair.getLeft());
					errors.addAll(twsePair.getRight());
					results.addAll(tpexPair.getLeft());
					errors.addAll(tpexPair.getRight());

				} catch (Exception e) {
					log.error("routine job error", e);

					return;
				}

				//獲取昨天
				Map<String, DailyStockInfoDto> formers = remoteStockService.getFormer(Long.parseLong(nowStr), null).getData();

				//清洗
				for (DailyStockInfoDto dto : results) {
					DailyStockInfoDto yesterdayDTO = formers.get(dto.getStockId());
					dto.setYesterdayClosingPrice(yesterdayDTO.getTodayClosingPrice());
					dto.setYesterdayTradingVolumePiece(yesterdayDTO.getTodayTradingVolumePiece());
					dto.setYesterdayTradingVolumeMoney(yesterdayDTO.getTodayTradingVolumeMoney());

					if (dto.getTodayClosingPrice() == null) {
						dto.setTodayClosingPrice(yesterdayDTO.getTodayClosingPrice());
						dto.setOpeningPrice(dto.getTodayClosingPrice());
						dto.setHighestPrice(dto.getTodayClosingPrice());
						dto.setLowestPrice(dto.getTodayClosingPrice());
						dto.setPriceGap(BigDecimal.ZERO);
						dto.setPriceGapPercent(BigDecimal.ZERO);
					} else {
						dto.setPriceGap(
								dto.getTodayClosingPrice().subtract(yesterdayDTO.getTodayClosingPrice())
						);
						dto.setPriceGapPercent(
								dto.getPriceGap().divide(yesterdayDTO.getTodayClosingPrice(), 4, RoundingMode.FLOOR).multiply(BigDecimal.valueOf(100))
						);
					}

					formers.remove(dto.getStockId());
				}

				//補漏
				formers.forEach((k, v) -> {
					DailyStockInfoDto result = new DailyStockInfoDto();
					result.setStockId(k);
					result.setStockName(v.getStockName());
					result.setDate(Long.parseLong(nowStr));
					results.add(result);
				});

				//獲取本日資料, 以防已經手動執行過任務
				Map<String, DailyStockInfoDto> stockIdToTodayInfo = remoteStockService.getByDate(Long.parseLong(nowStr), null).getData();

				results.forEach(dto -> {
					if (stockIdToTodayInfo.get(dto.getStockId()) != null) {
						dto.setId(stockIdToTodayInfo.get(dto.getStockId()).getId());
					}
				});

				if (!errors.isEmpty()) {
					errorMessageService.saveBatch(errors);
				}

				InnerResponse<ObjectUtils.Null> innerResponse = null;
				if (!results.isEmpty()) {
					innerResponse = remoteStockService.saveAll(results, null);
				}

				log.info("【TWSE-TPEX Routine 爬蟲結束】共抓取{}筆資料，入庫回應: {}", results.size(), JSON.toJSON(innerResponse));
			}
		} catch (InterruptedException e) {
			log.error("lock error", e);
		} finally {
			if (lock.isLocked()) {
				lock.unlock();
			}
		}
	}

	@XxlJob("real-time-price-scrape")
	public void realTimeHandle() {
		RLock lock = cacheService.getLock(CacheKeys.SCRAPE_INFO_CACHE.getLock());
		Long date = Long.parseLong(LocalDate.now().format(DatePattern.PURE_DATE_FORMATTER));
		try {
			if (lock.tryLock(5, 300, TimeUnit.SECONDS)) {
				List<Integer> listed = List.of(1, 2, 3, 4, 6, 7, 9, 10, 11, 12, 13, 19, 20, 21, 22, 24, 25, 30, 37, 38, 40, 41, 42, 43, 44, 45, 46, 47, 49, 93, 94, 95, 96);
				List<Integer> otc = List.of(97, 98, 121, 122, 123, 124, 125, 126, 130, 138, 139, 140, 141, 142, 145, 151, 153, 154, 155, 156, 157, 158, 159, 160, 161, 169, 170, 171);

				List<String> urls = new ArrayList<>();

				for (Integer num : listed) {
					urls.add(StrUtil.format("https://tw.stock.yahoo.com/class-quote?sectorId={}&exchange=TAI", num));
				}

				for (Integer num : otc) {
					urls.add(StrUtil.format("https://tw.stock.yahoo.com/class-quote?sectorId={}&exchange=TWO", num));
				}

				YahooRealTimeScraper scraper = new YahooRealTimeScraper(urls, date);
				YahooRealTimePipeline pipeline = new YahooRealTimePipeline();
				Spider.create(scraper)
						.addPipeline(pipeline)
						.addUrl(scraper.getFirstUrl())
						.setDownloader(
								new SeleniumDownloader(
										classpath + driverPath,
										Duration.of(15, ChronoUnit.SECONDS),
										ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//div[@class='table-body-wrapper']")),
										null
								)
						)
						.thread(2)
						.run();

				//取得所有股票即時報價
				List<DailyStockInfoDto> results = pipeline.getResults();

				//去重
				Map<String, DailyStockInfoDto> stockIdToTodayInfo = remoteStockService.getByDate(date, null).getData();
				results.forEach(dto -> {
					if (stockIdToTodayInfo.get(dto.getStockId()) != null) {
						dto.setId(stockIdToTodayInfo.get(dto.getStockId()).getId());
					}
				});

				remoteStockService.saveAll(results, null);
			}
		} catch (InterruptedException e) {
			log.error("lock error", e);

			return;
		} finally {
			if (lock.isLocked()) {
				lock.unlock();
			}
		}

		//計算metrics
		remoteReportService.genRealTimeMetricsReport(date, null);

		//計算tag(同上)
		remoteReportService.genRealTimeDetailReport(date, null);

	}
}
