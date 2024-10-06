package com.personal.project.scraperservice.job;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.scraperservice.constant.CacheKeys;
import com.personal.project.scraperservice.model.dto.DailyIndexInfoDTO;
import com.personal.project.scraperservice.model.dto.DailyStockInfoDTO;
import com.personal.project.scraperservice.model.dto.PyIndexDTO;
import com.personal.project.scraperservice.model.dto.PyStockDTO;
import com.personal.project.scraperservice.remote.RemotePythonService;
import com.personal.project.scraperservice.remote.RemoteReportService;
import com.personal.project.scraperservice.remote.RemoteStockService;
import com.personal.project.scraperservice.service.CacheService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.redisson.api.RLock;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@AllArgsConstructor
public class ScraperJob {

	private final CacheService cacheService;

	private final RemoteStockService remoteStockService;

	private final RemoteReportService remoteReportService;

	private final RemotePythonService remotePythonService;

	/**
	 * Docker容器已經替換為無瀏覽器版本, 現無法使用, 之後重構
	 */
//	@XxlJob("twse-tpex-system-init-scrape")
//	public void twseAndTPEXInitHandle() {
//		//檢查是否已經有資料存在
//		InnerResponse<Boolean> checkRes = remoteStockService.checkInit();
//		if (checkRes.getData()) {
//			log.error("已存在資料, 資料初始化任務 twse-tpex-system-init-scrape 不執行");
//			return;
//		}
//
//		RLock lock = cacheService.getLock(CacheKeys.INIT_DATA_CACHE.getLock());
//		try {
//			if (lock.tryLock(10, 3600, TimeUnit.SECONDS)) {
//				int offMonths = 6; //現在往前推幾個月～本月資料
//				LocalDate now = LocalDate.now();
//				log.info("資料初始化任務 twse-tpex-system-init-scrape 開始執行");
//				TimeInterval timer = DateUtil.timer();
//
//				//日期清單
//				List<LocalDate> dates = new ArrayList<>();
//				for (LocalDate i = now; !i.isBefore(i.minusMonths(offMonths)); i = i.minusDays(1)) {
//					if (i.getDayOfWeek() == DayOfWeek.SATURDAY || i.getDayOfWeek() == DayOfWeek.SUNDAY) {
//						continue;
//					}
//					dates.add(i);
//				}
//
//				try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
//					//獲取上市股票
//					Future<?> listedFuture = executorService.submit(() -> {
//						log.info("資料初始化任務 twse-tpex-system-init-scrape, 上市日期：{}-{}   爬蟲開始", dates.getFirst(), dates.getLast());
//
//						//產出下載地址
//						List<String> twseUrls = generateTWSEUrls(dates);
//
//						//爬蟲開始
//						TWSEInitScraper scraper = new TWSEInitScraper();
//						scraper.setUrls(twseUrls);
//						TWSEInitPipeline pipeline = new TWSEInitPipeline();
//
//						Map<String, List<DailyStockInfoDTO>> data = scrapeTWSEInitDailyStockInfo(scraper, pipeline);
//
//						data.forEach((k, v) -> v.sort(Comparator.comparingLong(DailyStockInfoDTO::getDate).reversed()));
//
//						//資料清洗
//						cleanupTWSEData(data);
//
//						//資料補全
//						completeTWSEData(data);
//
//						//填充
//						List<DailyStockInfoDTO> finalResults = fillData(data);
//
//						//資料入庫
//						InnerResponse<ObjectUtils.Null> response = remoteStockService.initSaveAll(finalResults);
//						log.info("資料初始化任務 twse-tpex-system-init-scrape 上市個股 入庫結束, {}", JSON.toJSON(response));
//					});
//
//					Future<?> otcFuture = executorService.submit(() -> {
//						log.info("資料初始化任務 twse-tpex-system-init-scrape, 上櫃爬蟲開始");
//
//						//產出下載地址
//						List<String> tpexUrls = generateTPEXUrls(dates);
//
//						TPEXInitScraper tpexInitScraper = new TPEXInitScraper(tpexUrls);
//						TPEXInitPipeline tpexInitPipeline = new TPEXInitPipeline();
//
//						//爬蟲開始
//						Map<String, List<DailyStockInfoDTO>> data = scrapeTPEXInitDailyStockInfo(tpexInitScraper, tpexInitPipeline);
//
//						data.forEach((k, v) -> v.sort(Comparator.comparingLong(DailyStockInfoDTO::getDate).reversed()));
//
//						//清洗
//						cleanupTPEXData(data);
//
//						//補全
//						completeTPEXData(data);
//
//						//填充
//						List<DailyStockInfoDTO> finalResults = fillData(data);
//
//						//資料入庫
//						InnerResponse<ObjectUtils.Null> response = remoteStockService.initSaveAll(finalResults);
//						log.info("資料初始化任務 twse-tpex-system-init-scrape 上櫃個股 入庫結束, {}", JSON.toJSON(response));
//					});
//
//					listedFuture.get();
//					otcFuture.get();
//
//				} catch (InterruptedException | ExecutionException e) {
//					log.error("", e);
//				}
//
//				log.info("資料初始化任務 twse-tpex-system-init-scrape 爬蟲部分結束, 花費 {} 毫秒", timer.intervalRestart());
//
//				//call remote report to cal yesterday metric and detail
//				InnerResponse<ObjectUtils.Null> response1 = remoteReportService.initYesterdayReport(null);
//				log.info("資料初始化任務 twse-tpex-system-init-scrape 初始化上一個交易日的指標報告/詳細報告花費 {} 毫秒, 結果 {}", timer.intervalRestart(), JSON.toJSON(response1));
//
//				//call remote report to cal newest trading day metric and detail
//				InnerResponse<ObjectUtils.Null> response2 = remoteReportService.initTodayReport(null);
//				log.info("資料初始化任務 twse-tpex-system-init-scrape 初始化最新交易日的指標報告/詳細報告花費 {} 毫秒, 結果 {}", timer.intervalRestart(), JSON.toJSON(response2));
//			}
//		} catch (InterruptedException e) {
//			log.error("lock error", e);
//		} finally {
//			if (lock.isLocked()) {
//				lock.unlock();
//			}
//		}
//
//		log.info("資料初始化任務 twse-tpex-system-init-scrape 結束");
//	}

//	/**
//	 * 產出TWSE個股日成交資訊地址
//	 *
//	 * @return
//	 */
//	private List<String> generateTWSEUrls(List<LocalDate> dates) {
//		List<String> twseUrls = new ArrayList<>();
//		for (LocalDate date : dates) {
//			twseUrls.add(StrUtil.format("https://www.twse.com.tw/rwd/zh/afterTrading/MI_INDEX?date={}&type=ALL&response=html", date.format(DatePattern.PURE_DATE_FORMATTER)));
//		}
//
//		return twseUrls;
//	}

//	/**
//	 * 爬TWSE(上市)個股日成交資訊
//	 *
//	 * @param twseInitScraper
//	 * @param twseInitPipeline
//	 * @return
//	 */
//	private Map<String, List<DailyStockInfoDTO>> scrapeTWSEInitDailyStockInfo(TWSEInitScraper twseInitScraper, TWSEInitPipeline twseInitPipeline) {
//		Spider.create(twseInitScraper)
//				.addUrl(twseInitScraper.getFirstUrl())
//				.addPipeline(twseInitPipeline)
//				.thread(2)
//				.run();
//
//		return twseInitPipeline.getResult();
//	}

//	/**
//	 * 清洗TWSE資料(補全高開低收)
//	 * TWSE資料於開高低收有可能為--, 此處替換為前一日收盤
//	 *
//	 * @param sortedData
//	 */
//	private void cleanupTWSEData(Map<String, List<DailyStockInfoDTO>> sortedData) {
//		sortedData.forEach((k, v) -> {
//			for (int i = 0; i < v.size(); i++) {
//				DailyStockInfoDTO dataLostDTO = v.get(i);
//				if (dataLostDTO.getTodayClosingPrice() == null) {
//					List<DailyStockInfoDTO> subList;
//					BigDecimal lastEffectivePrice = null;
//					if (i == 0) {
//						subList = v.subList(1, v.size());
//						lastEffectivePrice = subList.stream()
//								.filter(dto -> dto.getTodayClosingPrice() != null)
//								.findFirst()
//								.map(DailyStockInfoDTO::getTodayClosingPrice)
//								.orElse(lastEffectivePrice);
//						log.error("無效日期最新, 上市：{}, 日期：{}, 有效：{}", dataLostDTO.getStockId(), dataLostDTO.getDate(), lastEffectivePrice);
//					} else if (i == v.size() - 1) {
//						subList = v.subList(0, i).reversed();
//						lastEffectivePrice = subList.stream()
//								.filter(dto -> dto.getTodayClosingPrice() != null)
//								.findFirst()
//								.map(dto -> dto.getTodayClosingPrice().subtract(dto.getPriceGap()))
//								.orElse(lastEffectivePrice);
//						log.error("無效日期最舊, 上市：{}, 日期：{}, 有效：{}", dataLostDTO.getStockId(), dataLostDTO.getDate(), lastEffectivePrice);
//					} else {
//						subList = v.subList(i, v.size());
//						lastEffectivePrice = subList.stream()
//								.filter(dto -> dto.getTodayClosingPrice() != null)
//								.findFirst()
//								.map(DailyStockInfoDTO::getTodayClosingPrice)
//								.orElse(lastEffectivePrice);
//						if (lastEffectivePrice == null) {
//							subList = v.subList(0, i).reversed();
//							lastEffectivePrice = subList.stream()
//									.filter(dto -> dto.getTodayClosingPrice() != null)
//									.findFirst()
//									.map(dto -> dto.getTodayClosingPrice().subtract(dto.getPriceGap()))
//									.orElse(lastEffectivePrice);
//						}
//						log.error("無效日期中間, 上市：{}, 日期：{}, 有效：{}", dataLostDTO.getStockId(), dataLostDTO.getDate(), lastEffectivePrice);
//					}
//					if (lastEffectivePrice == null) {
//						log.error("上市沒救, 往前後找都ＧＧ, {}, {}", k, dataLostDTO.getDate());
//					}
//					dataLostDTO.setTodayClosingPrice(lastEffectivePrice);
//
//					if (dataLostDTO.getHighestPrice() == null) {
//						dataLostDTO.setHighestPrice(lastEffectivePrice);
//					}
//					if (dataLostDTO.getOpeningPrice() == null) {
//						dataLostDTO.setOpeningPrice(lastEffectivePrice);
//					}
//					if (dataLostDTO.getLowestPrice() == null) {
//						dataLostDTO.setLowestPrice(lastEffectivePrice);
//					}
//				}
//			}
//		});
//	}

//	/**
//	 * 補全TWSE資料(昨額 昨收 昨量 差額 漲跌幅)
//	 *
//	 * @param sortedData
//	 */
//	private void completeTWSEData(Map<String, List<DailyStockInfoDTO>> sortedData) {
//		sortedData.forEach((k, v) -> {
//			DailyStockInfoDTO tempDTO = null;
//			for (int i = 0; i < v.size(); i++) {
//				DailyStockInfoDTO thisRoundDTO = v.get(i);
//				//tempDTO填上缺失的昨日資訊
//				if (tempDTO != null) {
//					tempDTO.setYesterdayClosingPrice(thisRoundDTO.getTodayClosingPrice());
//					tempDTO.setYesterdayTradingVolumePiece(thisRoundDTO.getTodayTradingVolumePiece());
//					tempDTO.setYesterdayTradingVolumeMoney(thisRoundDTO.getTodayTradingVolumeMoney());
//					tempDTO.setPriceGap(tempDTO.getTodayClosingPrice().subtract(tempDTO.getYesterdayClosingPrice()));
//					tempDTO.setPriceGapPercent(tempDTO.getPriceGap().divide(tempDTO.getYesterdayClosingPrice(), 4, RoundingMode.FLOOR).multiply(BigDecimal.valueOf(100)));
//				}
//				tempDTO = thisRoundDTO;
//
//				if (i == v.size() - 1) {
//					thisRoundDTO.setPriceGapPercent(null);
//					thisRoundDTO.setPriceGap(null);
//					thisRoundDTO.setYesterdayClosingPrice(null);
//				}
//			}
//		});
//	}

//	/**
//	 * 填充平日但非交易日的資料
//	 */
//	private List<DailyStockInfoDTO> fillData(Map<String, List<DailyStockInfoDTO>> sortedData) {
//		List<DailyStockInfoDTO> finalResults = new ArrayList<>();
//		sortedData.forEach((k, v) -> {
//			DailyStockInfoDTO tempDTO = null;
//			for (int i = 0; i < v.size(); i++) {
//				DailyStockInfoDTO thisRoundDTO = v.get(i);
//				if (tempDTO != null) {
//					LocalDate tempDTODate = LocalDate.parse(tempDTO.getDate().toString(), DatePattern.PURE_DATE_FORMATTER);
//					LocalDate thisDate = LocalDate.parse(thisRoundDTO.getDate().toString(), DatePattern.PURE_DATE_FORMATTER);
//					if (!thisDate.isEqual(tempDTODate.minusDays(1)) &&
//							thisDate.getDayOfWeek() != DayOfWeek.FRIDAY) {
//						LocalDate flagDate = tempDTODate.minusDays(1);
//						while (!flagDate.isEqual(thisDate)) {
//							if (flagDate.getDayOfWeek() != DayOfWeek.SATURDAY && flagDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
//								DailyStockInfoDTO fillDTO = new DailyStockInfoDTO();
//								fillDTO.setStockId(k);
//								fillDTO.setStockName(thisRoundDTO.getStockName());
//								fillDTO.setMarket(thisRoundDTO.getMarket());
//								fillDTO.setDate(Long.valueOf(flagDate.format(DatePattern.PURE_DATE_FORMATTER)));
//								finalResults.add(fillDTO);
//							}
//							flagDate = flagDate.minusDays(1);
//						}
//					}
//				}
//				tempDTO = thisRoundDTO;
//			}
//			finalResults.addAll(v);
//		});
//
//		return finalResults;
//	}

//	/**
//	 * 爬TPEX(櫃買)個股日成交資訊
//	 *
//	 * @param tpexInitScraper
//	 * @param tpexInitPipeline
//	 * @return
//	 */
//	private Map<String, List<DailyStockInfoDTO>> scrapeTPEXInitDailyStockInfo(TPEXInitScraper tpexInitScraper, TPEXInitPipeline tpexInitPipeline) {
//		Spider.create(tpexInitScraper)
//				.addUrl(tpexInitScraper.getUrlsFirst())
//				.addPipeline(tpexInitPipeline)
//				.thread(2)
//				.run();
//
//		return tpexInitPipeline.getResult();
//	}

//	/**
//	 * 產出TPEX個股日成交資訊地址
//	 *
//	 * @return
//	 */
//	private List<String> generateTPEXUrls(List<LocalDate> dates) {
//		List<String> tpexUrls = new ArrayList<>();
//
//		for (LocalDate date : dates) {
//			tpexUrls.add(
//					StrUtil.format(
//							"https://www.tpex.org.tw/web/stock/aftertrading/otc_quotes_no1430/stk_wn1430_result.php?l=zh-tw&o=htm&d={}/{}/{}&se=AL&s=0,asc,0",
//							date.getYear() - 1911,
//							date.getMonth().getValue() < 10 ? "0" + date.getMonth().getValue() : date.getMonth().getValue(),
//							date.getDayOfMonth() < 10 ? "0" + date.getDayOfMonth() : date.getDayOfMonth()
//					)
//			);
//		}
//
//		return tpexUrls;
//	}

//	/**
//	 * 清洗TPEX資料(補全高開低收)
//	 * TPEX資料於開高低收有可能為--, 此處替換為前一日收盤
//	 *
//	 * @param sortedData
//	 */
//	private void cleanupTPEXData(Map<String, List<DailyStockInfoDTO>> sortedData) {
//		sortedData.forEach((k, v) -> {
//			for (int i = 0; i < v.size(); i++) {
//				DailyStockInfoDTO dataLostDTO = v.get(i);
//				if (dataLostDTO.getTodayClosingPrice() == null) {
//					List<DailyStockInfoDTO> subList;
//					BigDecimal lastEffectivePrice = null;
//					if (i == 0) {
//						subList = v.subList(1, v.size());
//						lastEffectivePrice = subList.stream()
//								.filter(dto -> dto.getTodayClosingPrice() != null)
//								.findFirst()
//								.map(DailyStockInfoDTO::getTodayClosingPrice)
//								.orElse(lastEffectivePrice);
//						log.error("無效日期最新, 上櫃：{}, 日期：{}, 有效：{}", dataLostDTO.getStockId(), dataLostDTO.getDate(), lastEffectivePrice);
//					} else if (i == v.size() - 1) {
//						subList = v.subList(0, i).reversed();
//						lastEffectivePrice = subList.stream()
//								.filter(dto -> dto.getTodayClosingPrice() != null)
//								.findFirst()
//								.map(dto -> dto.getTodayClosingPrice().subtract(dto.getPriceGap()))
//								.orElse(lastEffectivePrice);
//						log.error("無效日期最舊, 上櫃：{}, 日期：{}, 有效：{}", dataLostDTO.getStockId(), dataLostDTO.getDate(), lastEffectivePrice);
//					} else {
//						subList = v.subList(i, v.size());
//						lastEffectivePrice = subList.stream()
//								.filter(dto -> dto.getTodayClosingPrice() != null)
//								.findFirst()
//								.map(DailyStockInfoDTO::getTodayClosingPrice)
//								.orElse(lastEffectivePrice);
//						if (lastEffectivePrice == null) {
//							subList = v.subList(0, i).reversed();
//							lastEffectivePrice = subList.stream()
//									.filter(dto -> dto.getTodayClosingPrice() != null)
//									.findFirst()
//									.map(dto -> dto.getTodayClosingPrice().subtract(dto.getPriceGap()))
//									.orElse(lastEffectivePrice);
//						}
//						log.error("無效日期中間, 上櫃：{}, 日期：{}, 有效：{}", dataLostDTO.getStockId(), dataLostDTO.getDate(), lastEffectivePrice);
//					}
//					if (lastEffectivePrice == null) {
//						log.error("上櫃沒救, 往前後找都ＧＧ, {}, {}", k, dataLostDTO.getDate());
//					}
//					dataLostDTO.setTodayClosingPrice(lastEffectivePrice);
//
//					if (dataLostDTO.getHighestPrice() == null) {
//						dataLostDTO.setHighestPrice(lastEffectivePrice);
//					}
//					if (dataLostDTO.getOpeningPrice() == null) {
//						dataLostDTO.setOpeningPrice(lastEffectivePrice);
//					}
//					if (dataLostDTO.getLowestPrice() == null) {
//						dataLostDTO.setLowestPrice(lastEffectivePrice);
//					}
//				}
//			}
//		});
//	}

//	/**
//	 * 補全TPEX資料(差額, 昨收, 漲跌幅, 昨額 昨量)
//	 *
//	 * @param sortedData
//	 */
//	private void completeTPEXData(Map<String, List<DailyStockInfoDTO>> sortedData) {
//		sortedData.forEach((k, v) -> {
//			DailyStockInfoDTO tempDTO = null;
//			for (int i = 0; i < v.size(); i++) {
//				DailyStockInfoDTO thisRoundDTO = v.get(i);
//				//tempDTO填上缺失的昨日資訊
//				if (tempDTO != null) {
//					tempDTO.setYesterdayClosingPrice(thisRoundDTO.getTodayClosingPrice());
//					tempDTO.setYesterdayTradingVolumePiece(thisRoundDTO.getTodayTradingVolumePiece());
//					tempDTO.setYesterdayTradingVolumeMoney(thisRoundDTO.getTodayTradingVolumeMoney());
//					tempDTO.setPriceGap(tempDTO.getTodayClosingPrice().subtract(tempDTO.getYesterdayClosingPrice()));
//					tempDTO.setPriceGapPercent(tempDTO.getPriceGap().divide(tempDTO.getYesterdayClosingPrice(), 4, RoundingMode.FLOOR).multiply(BigDecimal.valueOf(100)));
//				}
//				tempDTO = thisRoundDTO;
//
//				if (i == v.size() - 1) {
//					thisRoundDTO.setPriceGapPercent(null);
//					thisRoundDTO.setPriceGap(null);
//					thisRoundDTO.setYesterdayClosingPrice(null);
//				}
//			}
//		});
//	}
	@XxlJob("real-time-price-scrape")
	public void realTimeHandle() {
		RLock lock = cacheService.getLock(CacheKeys.SCRAPE_INFO_CACHE.getLock());
		try {
			if (lock.tryLock(5, 300, TimeUnit.SECONDS)) {
				InnerResponse<List<PyStockDTO>> pyRes = remotePythonService.realtimePy();
				if (!"200".equals(pyRes.getCode()) || pyRes.getData() == null) {
					log.error("獲取yahoo即時服務錯誤, {}", pyRes.getMsg());
					return;
				} else if (pyRes.getData().isEmpty()) {
					return;
				}

				List<DailyStockInfoDTO> infos = pyRes.getData().stream()
						.map(e -> BeanUtil.copyProperties(e, DailyStockInfoDTO.class))
						.toList();

				//去重
				Map<String, DailyStockInfoDTO> existedInfos = remoteStockService.getByDate(infos.getFirst().getDate()).getData();
				infos.forEach(dto -> {
					if (existedInfos.get(dto.getStockId()) != null) {
						dto.setId(existedInfos.get(dto.getStockId()).getId());
					}
				});

				InnerResponse<ObjectUtils.Null> response = remoteStockService.saveAll(infos);

				if (!response.getCode().equals("200")) {
					log.error("yahoo即時任務儲存失敗, {}", response.getMsg());
					return;
				}

				//計算metrics
				remoteReportService.genRealTimeMetricsReport(infos.getFirst().getDate(), null);

				//計算tag(同上)
				remoteReportService.genRealTimeDetailReport(infos.getFirst().getDate(), null);

				log.info("處理yahoo即時服務結束");
			}
		} catch (InterruptedException e) {
			log.error("lock error", e);
		} finally {
			if (lock.isLocked()) {
				lock.unlock();
			}
		}
	}

	@XxlJob("twse-tpex-routine-scrape")
	public void twseJobHandle() {
		RLock lock = cacheService.getLock(CacheKeys.SCRAPE_INFO_CACHE.getLock());
		try {
			if (lock.tryLock(5, 30, TimeUnit.SECONDS)) {
				InnerResponse<List<PyStockDTO>> pyRes = remotePythonService.routinePy();
				if (!"200".equals(pyRes.getCode()) || pyRes.getData() == null) {
					log.error("獲取盤後資訊服務錯誤, {}", pyRes.getMsg());
					return;
				} else if (pyRes.getData().isEmpty()) {
					return;
				}

				List<DailyStockInfoDTO> infos = new ArrayList<>(pyRes.getData().stream()
						.map(e -> BeanUtil.copyProperties(e, DailyStockInfoDTO.class))
						.toList());

				//獲取昨天
				Map<String, DailyStockInfoDTO> formers = remoteStockService.getFormer(infos.getFirst().getDate()).getData();

				//清洗
				for (DailyStockInfoDTO dto : infos) {
					DailyStockInfoDTO yesterdayDTO = formers.get(dto.getStockId());
					if (yesterdayDTO == null) {//今天上市櫃
						dto.setYesterdayClosingPrice(dto.getTodayClosingPrice());
						dto.setYesterdayTradingVolumePiece(dto.getTodayTradingVolumePiece());
						dto.setYesterdayTradingVolumeMoney(dto.getTodayTradingVolumeMoney());
						dto.setPriceGap(BigDecimal.ZERO);
						dto.setPriceGapPercent(BigDecimal.ZERO);
					} else {
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
					}

					formers.remove(dto.getStockId());
				}

				//補漏
				formers.forEach((k, v) -> {
					DailyStockInfoDTO result = new DailyStockInfoDTO();
					result.setStockId(k);
					result.setStockName(v.getStockName());
					result.setDate(infos.getFirst().getDate());
					result.setMarket(v.getMarket());
					infos.add(result);
				});

				//獲取本日資料, 以防已經手動執行過任務
				Map<String, DailyStockInfoDTO> data = remoteStockService.getByDate(infos.getFirst().getDate()).getData();

				infos.forEach(dto -> {
					if (data.get(dto.getStockId()) != null) {
						dto.setId(data.get(dto.getStockId()).getId());
					}
				});

				InnerResponse<ObjectUtils.Null> innerResponse = remoteStockService.saveAll(infos);

				log.info("【TWSE-TPEX Routine 爬蟲結束】共抓取{}筆資料，入庫回應: {}", infos.size(), JSONUtil.toJsonStr(innerResponse));
			}
		} catch (InterruptedException e) {
			log.error("lock error", e);
		} finally {
			if (lock.isLocked()) {
				lock.unlock();
			}
		}
	}

	@XxlJob("yahoo-index-scrape")
	public void yahooIndexJobHandle() {
		InnerResponse<List<PyIndexDTO>> pyRes = remotePythonService.indexPy();
		if (!"200".equals(pyRes.getCode()) || pyRes.getData() == null) {
			log.error("獲取大盤資訊服務錯誤, {}", pyRes.getMsg());
			return;
		} else if (pyRes.getData().isEmpty()) {
			return;
		}

		List<DailyIndexInfoDTO> infos = pyRes.getData().stream()
				.map(e -> BeanUtil.copyProperties(e, DailyIndexInfoDTO.class))
				.toList();

		Map<String, DailyIndexInfoDTO> data = remoteStockService.getIndexByDate(infos.getFirst().getDate()).getData();
		infos.forEach(dto -> {
			if (data.get(dto.getIndexName()) != null) {
				dto.setId(data.get(dto.getIndexName()).getId());
			}
		});

		remoteStockService.saveAllIndex(infos);
	}
}
