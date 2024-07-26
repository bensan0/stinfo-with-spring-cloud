package com.personal.project.reportservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.personal.project.commoncore.constants.CommonTerm;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.reportservice.model.dto.*;
import com.personal.project.reportservice.remote.RemoteStockService;
import com.personal.project.reportservice.service.GenerateReportService;
import com.personal.project.reportservice.util.DailyDetailCalculator;
import com.personal.project.reportservice.util.DailyMetricsCalculator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class GenerateReportServiceImpl implements GenerateReportService {

	private final RemoteStockService remoteStockService;

	public GenerateReportServiceImpl(RemoteStockService remoteStockService) {
		this.remoteStockService = remoteStockService;
	}

	/**
	 * 系統資料初始化, 產生前一個交易日的指標報告
	 * 取得包含最新交易日, 最近241個交易日info資料計算
	 * 補全前一交易日與最新交易日之間非週六週日之未開市日資料
	 *
	 * @return
	 */
	@Override
	public InnerResponse<ObjectUtils.Null> generateInitYesterdayMetricReport() {
		Map<String, List<StockInfo4InitMetricsDTO>> idToDTOs = remoteStockService.get4CalInitYesterdayMetrics(null).getData();
		List<DailyStockMetricsDTO> results = new ArrayList<>();
		idToDTOs.forEach((k, v) -> {
			if (v.size() == 1) {
				return;
			}

			//第一個是最新交易日資料
			//產出上個交易日指標
			v.sort(Comparator.comparingLong(StockInfo4InitMetricsDTO::getDate).reversed());
			BigDecimal lastMA5Price = null;
			BigDecimal ma5 = null;
			try {
				ma5 = v.subList(1, 6).stream()
						.map(StockInfo4InitMetricsDTO::getTodayClosingPrice)
						.reduce(BigDecimal.ZERO, BigDecimal::add)
						.divide(BigDecimal.valueOf(5), 2, RoundingMode.FLOOR);
				lastMA5Price = v.get(5).getTodayClosingPrice();
			} catch (IndexOutOfBoundsException e) {
				log.error("個股 {} init yesterday metrics ma5天數不足", k);
			}

			BigDecimal ma10 = null;
			BigDecimal lastMA10Price = null;
			try {
				ma10 = v.subList(1, 11).stream()
						.map(StockInfo4InitMetricsDTO::getTodayClosingPrice)
						.reduce(BigDecimal.ZERO, BigDecimal::add)
						.divide(BigDecimal.valueOf(10), 2, RoundingMode.FLOOR);
				lastMA10Price = v.get(10).getTodayClosingPrice();
			} catch (IndexOutOfBoundsException e) {
				log.error("個股 {} init yesterday metrics ma10天數不足", k);
			}

			BigDecimal ma20 = null;
			BigDecimal lastMA20Price = null;
			try {
				ma20 = v.subList(1, 21).stream()
						.map(StockInfo4InitMetricsDTO::getTodayClosingPrice)
						.reduce(BigDecimal.ZERO, BigDecimal::add)
						.divide(BigDecimal.valueOf(20), 2, RoundingMode.FLOOR);
				lastMA20Price = v.get(20).getTodayClosingPrice();
			} catch (IndexOutOfBoundsException e) {
				log.error("個股 {} init yesterday metrics ma20天數不足", k);
			}

			BigDecimal ma60 = null;
			BigDecimal lastMA60Price = null;
			try {
				ma60 = v.subList(1, 61).stream()
						.map(StockInfo4InitMetricsDTO::getTodayClosingPrice)
						.reduce(BigDecimal.ZERO, BigDecimal::add)
						.divide(BigDecimal.valueOf(60), 2, RoundingMode.FLOOR);
				lastMA60Price = v.get(60).getTodayClosingPrice();
			} catch (IndexOutOfBoundsException e) {
				log.error("個股 {} init yesterday metrics ma60天數不足", k);
			}

			//todo 暫不計算 直至有方法快速收集歷史資料
//            BigDecimal ma120 = v.subList(1, 121).stream()
//                    .map(StockInfo4InitMetricsDTO::getTodayClosingPrice)
//                    .reduce(BigDecimal.ZERO, BigDecimal::add)
//                    .divide(BigDecimal.valueOf(120), 2, RoundingMode.FLOOR);
//            BigDecimal lastMA120Price = v.get(120).getTodayClosingPrice();
//
//            BigDecimal ma240 = v.subList(1, 241).stream()
//                    .map(StockInfo4InitMetricsDTO::getTodayClosingPrice)
//                    .reduce(BigDecimal.ZERO, BigDecimal::add)
//                    .divide(BigDecimal.valueOf(240), 2, RoundingMode.FLOOR);
//            BigDecimal lastMA240Price = v.get(240).getTodayClosingPrice();

			DailyStockMetricsDTO dto = DailyStockMetricsDTO.builder()
					.stockId(k)
					.stockName(v.getFirst().getStockName())
					.date(v.get(1).getDate())
					.todayClosingPrice(v.get(1).getTodayClosingPrice())
					.ma5(ma5)
					.lastMA5price(lastMA5Price)
					.ma10(ma10)
					.lastMA10price(lastMA10Price)
					.ma20(ma20)
					.lastMA20price(lastMA20Price)
					.ma60(ma60)
					.lastMA60price(lastMA60Price)
//                    .ma120(ma120)
//                    .lastMA120price(lastMA120Price)
//                    .ma240(ma240)
//                    .lastMA240price(lastMA240Price)
					.build();

			//上一個交易日與最新交易日之間若非隔日, 則填補兩日之間非週六週日的資料
			LocalDate newestTradingDate = LocalDate.parse(v.getFirst().getDate().toString(), DatePattern.PURE_DATE_FORMATTER);
			LocalDate lastTradingDate = LocalDate.parse(v.get(1).getDate().toString(), DatePattern.PURE_DATE_FORMATTER);

			if (!newestTradingDate.minusDays(1).isEqual(lastTradingDate)) {
				LocalDate tempDate = lastTradingDate.plusDays(1);
				while (tempDate.isBefore(newestTradingDate)) {
					if (tempDate.getDayOfWeek() != DayOfWeek.SATURDAY && tempDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
						DailyStockMetricsDTO copied = BeanUtil.copyProperties(dto, DailyStockMetricsDTO.class);
						copied.setDate(Long.parseLong(tempDate.format(DatePattern.PURE_DATE_FORMATTER)));
						copied.setTodayClosingPrice(null);
						results.add(copied);
					}
					tempDate = tempDate.plusDays(1);
				}
			}

			results.add(dto);
		});

		return remoteStockService.saveMetrics(results, null);
	}

	/**
	 * 系統資料初始化, 產生前一個交易日的細節報告(不含extra tags)
	 * 取得包含最新交易日, 不包含最新交易日最近11筆交易日info
	 * 填充上個交易日與最新交易日間非週六週日資料
	 *
	 * @return
	 */
	@Override
	public InnerResponse<ObjectUtils.Null> generateInitYesterdayDetailReport() {
		//最新交易日info +不含最新交易日 5個交易日份info
		InnerResponse<Map<String, List<StockInfo4InitDetailDTO>>> response = remoteStockService.get4CalInitYesterdayDetail(null);
		Map<String, List<StockInfo4InitDetailDTO>> stockIdToInfoDTOs = response.getData();
		List<DailyStockInfoDetailDTO> result = new ArrayList<>();

		stockIdToInfoDTOs.forEach((k, v) -> {
			if (v.size() == 1) {
				return;
			}
			v.sort(Comparator.comparingLong(StockInfo4InitDetailDTO::getDate).reversed());
			//第二筆是上個交易日資料
			StockInfo4InitDetailDTO second = v.get(1);
			DailyStockInfoDetailDTO detail = new DailyStockInfoDetailDTO();
			detail.setStockId(k);
			detail.setDate(second.getDate());
			detail.setTodayClosingPrice(second.getTodayClosingPrice());

			//影線與實體計算
			BigDecimal range = second.getHighestPrice().subtract(second.getLowestPrice());
			if (range.compareTo(BigDecimal.ZERO) != 0) {
				BigDecimal upperShadow = second.getHighestPrice()
						.subtract(second.getOpeningPrice().max(second.getTodayClosingPrice()))
						.divide(range, 4, RoundingMode.FLOOR)
						.multiply(BigDecimal.valueOf(100));
				detail.setUpperShadow(upperShadow);

				BigDecimal realBody = second.getOpeningPrice().subtract(second.getTodayClosingPrice())
						.abs()
						.divide(range, 4, RoundingMode.FLOOR)
						.multiply(BigDecimal.valueOf(100));
				detail.setRealBody(realBody);

				BigDecimal lowerShadow = second.getOpeningPrice().min(second.getTodayClosingPrice())
						.subtract(second.getLowestPrice())
						.divide(range, 4, RoundingMode.FLOOR)
						.multiply(BigDecimal.valueOf(100));
				detail.setLowerShadow(lowerShadow);
			} else {
				detail.setLowerShadow(BigDecimal.ZERO);
				detail.setRealBody(BigDecimal.ZERO);
				detail.setUpperShadow(BigDecimal.ZERO);
			}

			//連漲連跌計算
			String[] tempConsecutivePrice = new String[]{"1", CommonTerm.UNCHANGED};
			BigDecimal tempPrice = BigDecimal.ZERO;
			String[] priceStatus = new String[]{CommonTerm.UNCHANGED, CommonTerm.UNCHANGED};
			String[] tempConsecutiveTradingVolume = new String[]{"1", CommonTerm.UNCHANGED};
			BigDecimal tempVolume = BigDecimal.ZERO;
			String[] tempConsecutiveTradingAmount = new String[]{"1", CommonTerm.UNCHANGED};
			BigDecimal tempAmount = BigDecimal.ZERO;
			for (int i = v.size() - 1; i >= 1; i--) {
				StockInfo4InitDetailDTO dto = v.get(i);
				String st;
				//今比暫
				int priceDiff = dto.getTodayClosingPrice().compareTo(tempPrice);
				if (priceDiff > 0) {
					st = CommonTerm.RISE;
				} else if (priceDiff < 0) {
					st = CommonTerm.FALL;
				} else {
					st = CommonTerm.UNCHANGED;
				}
				if (st.equals(tempConsecutivePrice[1])) {
					tempConsecutivePrice[0] = String.valueOf(Integer.parseInt(tempConsecutivePrice[0]) + 1);
				} else {
					tempConsecutivePrice[0] = "1";
					tempConsecutivePrice[1] = st;
				}
				priceStatus[0] = priceStatus[1];
				priceStatus[1] = st;

				int volumeDiff = dto.getTodayTradingVolumePiece().compareTo(tempVolume);
				if (volumeDiff > 0) {
					st = CommonTerm.RISE;
				} else if (volumeDiff < 0) {
					st = CommonTerm.FALL;
				} else {
					st = CommonTerm.UNCHANGED;
				}
				if (tempConsecutiveTradingVolume[1].equals(st)) {
					tempConsecutiveTradingVolume[0] = String.valueOf(Integer.parseInt(tempConsecutiveTradingVolume[0]) + 1);
				} else {
					tempConsecutiveTradingVolume[0] = "1";
					tempConsecutiveTradingVolume[1] = st;
				}

				int amountDiff = dto.getTodayTradingVolumeMoney().compareTo(tempAmount);
				if (amountDiff > 0) {
					st = CommonTerm.RISE;
				} else if (amountDiff < 0) {
					st = CommonTerm.FALL;
				} else {
					st = CommonTerm.UNCHANGED;
				}
				if (tempConsecutiveTradingAmount[1].equals(st)) {
					tempConsecutiveTradingAmount[0] = String.valueOf(Integer.parseInt(tempConsecutiveTradingAmount[0]) + 1);
				} else {
					tempConsecutiveTradingAmount[0] = "1";
					tempConsecutiveTradingAmount[1] = st;
				}

				tempPrice = dto.getTodayClosingPrice();
				tempVolume = dto.getTodayTradingVolumePiece();
				tempAmount = dto.getTodayTradingVolumeMoney();
			}

			DailyStockInfoDetailDTO.TagsDTO tags = new DailyStockInfoDetailDTO.TagsDTO();
			tags.setConsecutivePrice(tempConsecutivePrice);
			tags.setConsecutiveTradingVolume(tempConsecutiveTradingVolume);
			tags.setConsecutiveTradingAmount(tempConsecutiveTradingAmount);
			tags.setPriceStatus(StrUtil.format("{}->{}", priceStatus[0], priceStatus[1]));

			//對比三天前
			try {
				BigDecimal vs2daysPriceDiff = second.getTodayClosingPrice()
						.subtract(v.get(3).getTodayClosingPrice())
						.divide(v.get(3).getTodayClosingPrice(), 4, RoundingMode.FLOOR)
						.multiply(BigDecimal.valueOf(100));
				if (vs2daysPriceDiff.compareTo(BigDecimal.ZERO) > 0) {
					tags.setPriceVS2DaysAgo(new String[]{CommonTerm.RISE, vs2daysPriceDiff.abs().toPlainString()});
				} else if (vs2daysPriceDiff.compareTo(BigDecimal.ZERO) < 0) {
					tags.setPriceVS2DaysAgo(new String[]{CommonTerm.FALL, vs2daysPriceDiff.abs().toPlainString()});
				} else {
					tags.setPriceVS2DaysAgo(new String[]{CommonTerm.UNCHANGED, vs2daysPriceDiff.abs().toPlainString()});
				}
				BigDecimal vs2daysVolumeDiff = second.getTodayTradingVolumePiece()
						.subtract(v.get(3).getTodayTradingVolumePiece())
						.divide(
								v.get(3).getTodayTradingVolumePiece().compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : v.get(3).getTodayTradingVolumePiece(),
								4,
								RoundingMode.FLOOR
						)
						.multiply(BigDecimal.valueOf(100));
				if (vs2daysVolumeDiff.compareTo(BigDecimal.ZERO) > 0) {
					tags.setTradingVolumeVS2DaysAgo(new String[]{CommonTerm.RISE, vs2daysVolumeDiff.abs().toPlainString()});
				} else if (vs2daysVolumeDiff.compareTo(BigDecimal.ZERO) < 0) {
					tags.setTradingVolumeVS2DaysAgo(new String[]{CommonTerm.FALL, vs2daysVolumeDiff.abs().toPlainString()});
				} else {
					tags.setTradingVolumeVS2DaysAgo(new String[]{CommonTerm.UNCHANGED, vs2daysVolumeDiff.abs().toPlainString()});
				}
				BigDecimal vs2daysAmountDiff = second.getTodayTradingVolumeMoney()
						.subtract(v.get(3).getTodayTradingVolumeMoney())
						.divide(
								v.get(3).getTodayTradingVolumeMoney().compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : v.get(3).getTodayTradingVolumeMoney(),
								4,
								RoundingMode.FLOOR
						)
						.multiply(BigDecimal.valueOf(100));
				if (vs2daysAmountDiff.compareTo(BigDecimal.ZERO) > 0) {
					tags.setTradingAmountVS2DaysAgo(new String[]{CommonTerm.RISE, vs2daysAmountDiff.abs().toPlainString()});
				} else if (vs2daysAmountDiff.compareTo(BigDecimal.ZERO) < 0) {
					tags.setTradingAmountVS2DaysAgo(new String[]{CommonTerm.FALL, vs2daysAmountDiff.abs().toPlainString()});
				} else {
					tags.setTradingAmountVS2DaysAgo(new String[]{CommonTerm.UNCHANGED, vs2daysAmountDiff.abs().toPlainString()});
				}
			} catch (IndexOutOfBoundsException e) {

			}

			//對比五天前
			try {
				BigDecimal vs4daysPriceDiff = second.getTodayClosingPrice()
						.subtract(v.get(5).getTodayClosingPrice())
						.divide(v.get(5).getTodayClosingPrice(), 4, RoundingMode.FLOOR)
						.multiply(BigDecimal.valueOf(100));
				if (vs4daysPriceDiff.compareTo(BigDecimal.ZERO) > 0) {
					tags.setPriceVS4DaysAgo(new String[]{CommonTerm.RISE, vs4daysPriceDiff.abs().toPlainString()});
				} else if (vs4daysPriceDiff.compareTo(BigDecimal.ZERO) < 0) {
					tags.setPriceVS4DaysAgo(new String[]{CommonTerm.FALL, vs4daysPriceDiff.abs().toPlainString()});
				} else {
					tags.setPriceVS4DaysAgo(new String[]{CommonTerm.UNCHANGED, vs4daysPriceDiff.abs().toPlainString()});
				}
				BigDecimal vs4daysVolumeDiff = second.getTodayTradingVolumePiece()
						.subtract(v.get(5).getTodayTradingVolumePiece())
						.divide(
								v.get(5).getTodayTradingVolumePiece().compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : v.get(5).getTodayTradingVolumePiece(),
								4,
								RoundingMode.FLOOR
						)
						.multiply(BigDecimal.valueOf(100));
				if (vs4daysVolumeDiff.compareTo(BigDecimal.ZERO) > 0) {
					tags.setTradingVolumeVS4DaysAgo(new String[]{CommonTerm.RISE, vs4daysVolumeDiff.abs().toPlainString()});
				} else if (vs4daysVolumeDiff.compareTo(BigDecimal.ZERO) < 0) {
					tags.setTradingVolumeVS4DaysAgo(new String[]{CommonTerm.FALL, vs4daysVolumeDiff.abs().toPlainString()});
				} else {
					tags.setTradingVolumeVS4DaysAgo(new String[]{CommonTerm.UNCHANGED, vs4daysVolumeDiff.abs().toPlainString()});
				}
				BigDecimal vs4daysAmountDiff = second.getTodayTradingVolumeMoney()
						.subtract(v.get(5).getTodayTradingVolumeMoney())
						.divide(
								v.get(5).getTodayTradingVolumeMoney().compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : v.get(5).getTodayTradingVolumeMoney(),
								4,
								RoundingMode.FLOOR
						)
						.multiply(BigDecimal.valueOf(100));
				if (vs4daysAmountDiff.compareTo(BigDecimal.ZERO) > 0) {
					tags.setTradingAmountVS4DaysAgo(new String[]{CommonTerm.RISE, vs4daysAmountDiff.abs().toPlainString()});
				} else if (vs4daysAmountDiff.compareTo(BigDecimal.ZERO) < 0) {
					tags.setTradingAmountVS4DaysAgo(new String[]{CommonTerm.FALL, vs4daysAmountDiff.abs().toPlainString()});
				} else {
					tags.setTradingAmountVS4DaysAgo(new String[]{CommonTerm.UNCHANGED, vs4daysAmountDiff.abs().toPlainString()});
				}
			} catch (IndexOutOfBoundsException e) {

			}

			detail.setTags(tags);

			//填充前一個交易日到最新交易日之間非週六週日資料
			LocalDate newestTradingDate = LocalDate.parse(String.valueOf(v.getFirst().getDate()), DatePattern.PURE_DATE_FORMATTER);
			LocalDate tempDate = LocalDate.parse(String.valueOf(v.get(1).getDate()), DatePattern.PURE_DATE_FORMATTER).plusDays(1);
			while (tempDate.isBefore(newestTradingDate)) {
				if (tempDate.getDayOfWeek() != DayOfWeek.SATURDAY && tempDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
					DailyStockInfoDetailDTO copied = BeanUtil.copyProperties(detail, DailyStockInfoDetailDTO.class);
					copied.setDate(Long.parseLong(tempDate.format(DatePattern.PURE_DATE_FORMATTER)));
					copied.setTodayClosingPrice(null);
					result.add(copied);
				}
				tempDate = tempDate.plusDays(1);
			}
			result.add(detail);
		});

		return remoteStockService.saveDetail(result, null);
	}

	/**
	 * 系統資料初始化, 產生最新交易日的指標報告
	 * 取得最新交易日以及最新交易日往前第5, 10, 20, 60, 120, 240個交易日info
	 * 取得前一個交易日指標報告(以init時的環境, 等同最新一份metric)
	 * 填充最新交易日到今日之間非週六週日資料
	 *
	 * @return
	 */
	@Override
	public InnerResponse<ObjectUtils.Null> generateInitTodayMetricReport() {

		InnerResponse<Query4CalInitTodayMetricsDTO> response = remoteStockService.get4CalInitTodayMetrics(null);
		Map<String, List<StockInfo4InitMetricsDTO>> stockIdToInfoDTOs = response.getData().getStockIdToInfoDTOs();
		Map<String, DailyStockMetricsDTO> stockIdToMetricsDTO = response.getData().getStockIdToMetricsDTO();
		List<DailyStockMetricsDTO> results = new ArrayList<>();
		LocalDate now = LocalDate.now();
		DailyMetricsCalculator metricsCalculator = new DailyMetricsCalculator(remoteStockService);

		stockIdToInfoDTOs.forEach((k, v) -> {
			v.sort(Comparator.comparingLong(StockInfo4InitMetricsDTO::getDate).reversed());
			StockInfo4InitMetricsDTO first = v.getFirst();//最新交易日info
			DailyStockMetricsDTO yesterdayMetrics = stockIdToMetricsDTO.get(k);
			DailyStockMetricsDTO result = new DailyStockMetricsDTO();
			result.setStockId(k);
			result.setStockName(first.getStockName());
			result.setDate(first.getDate());
			result.setTodayClosingPrice(first.getTodayClosingPrice());

			StockInfo4InitMetricsDTO ago4DaysInfo = null;
			try {
				ago4DaysInfo = v.get(1);
			} catch (Exception ignored) {
			}

			StockInfo4InitMetricsDTO ago9DaysInfo = null;
			try {
				ago9DaysInfo = v.get(2);
			} catch (Exception ignored) {
			}

			StockInfo4InitMetricsDTO ago19DaysInfo = null;
			try {
				ago19DaysInfo = v.get(3);
			} catch (Exception ignored) {
			}

			StockInfo4InitMetricsDTO ago59DaysInfo = null;
			try {
				ago59DaysInfo = v.get(4);
			} catch (Exception ignored) {
			}

			if (yesterdayMetrics != null) {
				metricsCalculator.cal(
						result,
						v.getFirst(),
						ago4DaysInfo,
						ago9DaysInfo,
						ago19DaysInfo,
						ago59DaysInfo,
//                    v.get(5),
//                    v.get(6),
						yesterdayMetrics
				);
			}

			//填充本日到最新交易日之間非週六週日的資料
			LocalDate tempDate = LocalDate.parse(first.getDate().toString(), DatePattern.PURE_DATE_FORMATTER).plusDays(1);
			while (tempDate.isBefore(now)) {
				if (tempDate.getDayOfWeek() != DayOfWeek.SATURDAY && tempDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
					DailyStockMetricsDTO copied = BeanUtil.copyProperties(result, DailyStockMetricsDTO.class);
					copied.setDate(Long.parseLong(tempDate.format(DatePattern.PURE_DATE_FORMATTER)));
					copied.setTodayClosingPrice(null);
					results.add(copied);
				}
				tempDate = tempDate.plusDays(1);
			}
			results.add(result);
		});

		return remoteStockService.saveMetrics(results, null);
	}

	/**
	 * 系統資料初始化, 產生最新交易日的詳細報告
	 * 取得最新交易日以及最新交易日往前第2, 3, 5個交易日info
	 * 取得最新交易日, 前一個交易日指標報告
	 * 取得前一個交易日詳細報告
	 * 填充最新交易日到今日之間非週六週日資料
	 *
	 * @return
	 */
	@Override
	public InnerResponse<ObjectUtils.Null> generateInitTodayDetailReport() {
		InnerResponse<Query4CalInitTodayDetailDTO> response = remoteStockService.get4CalInitTodayDetail(null);
		Map<String, List<DailyStockInfoDTO>> stockIdToInfoDTOs = response.getData().getStockIdToInfoDTOs();
		Map<String, List<DailyStockMetricsDTO>> stockIdToMetricsDTOs = response.getData().getStockIdToMetricsDTOs();
		Map<String, DailyStockInfoDetailDTO> stockIdToDetailDTO = response.getData().getStockIdToDetailDTO();
		List<DailyStockInfoDetailDTO> results = new ArrayList<>();

		DailyDetailCalculator detailCalculator = new DailyDetailCalculator();

		stockIdToInfoDTOs.forEach((k, v) -> {
			v.sort(Comparator.comparingLong(DailyStockInfoDTO::getDate).reversed());
			stockIdToMetricsDTOs.get(k).sort(Comparator.comparingLong(DailyStockMetricsDTO::getDate).reversed());
			DailyStockInfoDetailDTO detail = new DailyStockInfoDetailDTO();
			detail.setStockId(k);
			detail.setDate(v.getFirst().getDate());
			detail.setTodayClosingPrice(v.getFirst().getTodayClosingPrice());

			DailyStockInfoDTO yesterdayInfo = null;
			try {
				yesterdayInfo = v.get(1);
			} catch (Exception ignored) {
			}

			DailyStockInfoDTO twoDaysAgoInfo = null;
			try {
				twoDaysAgoInfo = v.get(2);
			} catch (Exception ignored) {
			}

			DailyStockInfoDTO fourDaysAgoInfo = null;
			try {
				fourDaysAgoInfo = v.get(3);
			} catch (Exception ignored) {
			}

			DailyStockMetricsDTO todayMetrics = null;
			try {
				todayMetrics = stockIdToMetricsDTOs.get(k).getFirst();
			} catch (Exception ignored) {
			}

			DailyStockMetricsDTO yesterdayMetrics = null;
			try {
				yesterdayMetrics = stockIdToMetricsDTOs.get(k).getLast();
			} catch (Exception ignored) {
			}

			DailyStockInfoDetailDTO yesterdayDetail = null;
			try {
				yesterdayDetail = stockIdToDetailDTO.get(k);
			} catch (Exception ignored) {
			}

			detailCalculator.cal(
					detail,
					v.getFirst(),
					yesterdayInfo,
					twoDaysAgoInfo,
					fourDaysAgoInfo,
					todayMetrics,
					yesterdayMetrics,
					yesterdayDetail
			);

			//填充交易日治本日之間非週六週日的資料
			LocalDate now = LocalDate.now();
			LocalDate tempDate = LocalDate.parse(v.getFirst().getDate().toString(), DatePattern.PURE_DATE_FORMATTER).plusDays(1);
			while (tempDate.isBefore(now)) {
				if (tempDate.getDayOfWeek() != DayOfWeek.SATURDAY && tempDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
					DailyStockInfoDetailDTO copied = BeanUtil.copyProperties(detail, DailyStockInfoDetailDTO.class);
					copied.setDate(Long.parseLong(tempDate.format(DatePattern.PURE_DATE_FORMATTER)));
					copied.setTodayClosingPrice(null);
					results.add(copied);
				}

				tempDate = tempDate.plusDays(1);
			}

			results.add(detail);
		});


		return remoteStockService.saveDetail(results, null);
	}

	public InnerResponse<ObjectUtils.Null> generateRoutineMetrics(Long date) {
		InnerResponse<CalMetricsUnionDTO> response = remoteStockService.getCalMetricsInfo(new Query4CalMetricsDTO(date), null);
		Map<String, List<DailyStockMetricsDTO>> stockIdToMetrics = response.getData().getStockIdToMetrics();
		Map<String, List<DailyStockInfoDTO>> stockIdToInfos = response.getData().getStockIdToInfos();
		List<DailyStockMetricsDTO> results = new ArrayList<>();
		DailyMetricsCalculator metricsCalculator = new DailyMetricsCalculator(remoteStockService);

		stockIdToInfos.forEach((k, v) -> {
			v.sort(Comparator.comparingLong(DailyStockInfoDTO::getDate).reversed());
			List<DailyStockMetricsDTO> metrics = stockIdToMetrics.get(k) == null ? Collections.emptyList() : stockIdToMetrics.get(k);
			metrics.sort(Comparator.comparingLong(DailyStockMetricsDTO::getDate).reversed());
			DailyStockMetricsDTO todayMetrics = null;
			DailyStockMetricsDTO yesterdayMetrics = null;

			//0 = 沒提前按 沒昨天(今天剛剛上市櫃)
			//1 = 沒提前按 有昨天 or 提前按 沒昨天
			//2 = 提前按 有昨天
			if (metrics.isEmpty()) {
				todayMetrics = new DailyStockMetricsDTO();
				todayMetrics.setStockId(k);
				todayMetrics.setStockName(v.getFirst().getStockName());
				todayMetrics.setDate(date);
			} else if (metrics.size() == 1) {
				if (metrics.getFirst().getDate().longValue() != date) {
					todayMetrics = new DailyStockMetricsDTO();
					todayMetrics.setStockId(k);
					todayMetrics.setStockName(v.getFirst().getStockName());
					todayMetrics.setDate(date);
					yesterdayMetrics = metrics.getFirst();
				} else {
					todayMetrics = metrics.getFirst();
				}
			} else {
				todayMetrics = metrics.getFirst();
				yesterdayMetrics = metrics.getLast();
			}

			todayMetrics.setTodayClosingPrice(v.getFirst().getTodayClosingPrice());

			//今日上市櫃
			if (v.size() == 1) {
				results.add(todayMetrics);
				return;
			}

			//此個股今天沒開市
			if (v.getFirst().getTodayClosingPrice() == null) {
				todayMetrics.setTodayClosingPrice(null);
				todayMetrics.setMa5(yesterdayMetrics.getMa5());
				todayMetrics.setLastMA5price(yesterdayMetrics.getLastMA5price());
				todayMetrics.setMa10(yesterdayMetrics.getMa10());
				todayMetrics.setLastMA10price(yesterdayMetrics.getLastMA10price());
				todayMetrics.setMa20(yesterdayMetrics.getMa20());
				todayMetrics.setLastMA20price(yesterdayMetrics.getLastMA20price());
				todayMetrics.setMa60(yesterdayMetrics.getMa60());
				todayMetrics.setLastMA60price(yesterdayMetrics.getLastMA60price());
				todayMetrics.setMa120(yesterdayMetrics.getMa120());
				todayMetrics.setLastMA120price(yesterdayMetrics.getLastMA120price());
				todayMetrics.setMa240(yesterdayMetrics.getMa240());
				todayMetrics.setLastMA240price(yesterdayMetrics.getLastMA240price());
				results.add(todayMetrics);
				return;
			}

			//因yesterday metrics一旦有值為null, 則往後的metrics並不會重新計算而導致該值永遠為null, 因此先行檢查,
			//若有發現值為null, 則以重新獲取過去info計算本日metrics
			if (yesterdayMetrics.getMa5() == null || yesterdayMetrics.getMa10() == null || yesterdayMetrics.getMa20() == null || yesterdayMetrics.getMa60() == null) {
				DailyStockMetricsDTO dailyStockMetricsDTO = generateRoutineMetricsOldSchool(k, date);
				dailyStockMetricsDTO.setId(todayMetrics.getId());
				todayMetrics = dailyStockMetricsDTO;
			} else {
				metricsCalculator.cal(todayMetrics,
						BeanUtil.copyProperties(v.getFirst(), StockInfo4InitMetricsDTO.class),
						BeanUtil.copyProperties(v.get(1), StockInfo4InitMetricsDTO.class),
						BeanUtil.copyProperties(v.get(2), StockInfo4InitMetricsDTO.class),
						BeanUtil.copyProperties(v.get(3), StockInfo4InitMetricsDTO.class),
						BeanUtil.copyProperties(v.get(4), StockInfo4InitMetricsDTO.class),
//                    BeanUtil.copyProperties(v.get(5), StockInfo4InitMetricsDTO.class),
//                    BeanUtil.copyProperties(v.getLast(), StockInfo4InitMetricsDTO.class),
						yesterdayMetrics);
			}

			results.add(todayMetrics);
		});

		//call feign save
		return remoteStockService.saveMetrics(results, null);
	}

	@Override
	public InnerResponse<ObjectUtils.Null> generateRealTimeMetrics(Long date) {
		InnerResponse<CalMetricsUnionDTO> response = remoteStockService.get4CalRealTimeMetrics(date, null);
		Map<String, List<DailyStockMetricsDTO>> stockIdToMetrics = response.getData().getStockIdToMetrics();
		Map<String, List<DailyStockInfoDTO>> stockIdToInfos = response.getData().getStockIdToInfos();
		List<DailyStockMetricsDTO> results = new ArrayList<>();
		DailyMetricsCalculator metricsCalculator = new DailyMetricsCalculator(remoteStockService);

		stockIdToInfos.forEach((k, v) -> {
			v.sort(Comparator.comparingLong(DailyStockInfoDTO::getDate).reversed());
			List<DailyStockMetricsDTO> metrics = stockIdToMetrics.get(k) == null ? Collections.emptyList() : stockIdToMetrics.get(k);
			metrics.sort(Comparator.comparingLong(DailyStockMetricsDTO::getDate).reversed());
			DailyStockMetricsDTO todayMetrics = null;
			DailyStockMetricsDTO yesterdayMetrics = null;

			//0 = 沒提前按 沒昨天(今天剛剛上市櫃)
			//1 = 沒提前按 有昨天 or 提前按 沒昨天
			//2 = 提前按 有昨天
			if (metrics.isEmpty()) {
				todayMetrics = new DailyStockMetricsDTO();
				todayMetrics.setStockId(k);
				todayMetrics.setStockName(v.getFirst().getStockName());
				todayMetrics.setDate(date);
			} else if (metrics.size() == 1) {
				if (metrics.getFirst().getDate().longValue() != date) {
					todayMetrics = new DailyStockMetricsDTO();
					todayMetrics.setStockId(k);
					todayMetrics.setStockName(v.getFirst().getStockName());
					todayMetrics.setDate(date);
					yesterdayMetrics = metrics.getFirst();
				} else {
					todayMetrics = metrics.getFirst();
				}
			} else {
				todayMetrics = metrics.getFirst();
				yesterdayMetrics = metrics.getLast();
			}

			todayMetrics.setTodayClosingPrice(v.getFirst().getTodayClosingPrice());

			//今日上市櫃
			if (v.size() == 1) {
				results.add(todayMetrics);
				return;
			}

			//此個股今天沒開市
			if (v.getFirst().getTodayClosingPrice() == null) {
				todayMetrics.setTodayClosingPrice(null);
				todayMetrics.setMa5(yesterdayMetrics.getMa5());
				todayMetrics.setLastMA5price(yesterdayMetrics.getLastMA5price());
				todayMetrics.setMa10(yesterdayMetrics.getMa10());
				todayMetrics.setLastMA10price(yesterdayMetrics.getLastMA10price());
				todayMetrics.setMa20(yesterdayMetrics.getMa20());
				todayMetrics.setLastMA20price(yesterdayMetrics.getLastMA20price());
				todayMetrics.setMa60(yesterdayMetrics.getMa60());
				todayMetrics.setLastMA60price(yesterdayMetrics.getLastMA60price());
				todayMetrics.setMa120(yesterdayMetrics.getMa120());
				todayMetrics.setLastMA120price(yesterdayMetrics.getLastMA120price());
				todayMetrics.setMa240(yesterdayMetrics.getMa240());
				todayMetrics.setLastMA240price(yesterdayMetrics.getLastMA240price());
				results.add(todayMetrics);
				return;
			}

			//因yesterday metrics一旦有值為null, 則往後的metrics並不會重新計算而導致該值永遠為null, 因此先行檢查,
			//若有發現值為null, 則以重新獲取過去info計算本日metrics
			if (yesterdayMetrics.getMa5() == null || yesterdayMetrics.getMa10() == null || yesterdayMetrics.getMa20() == null || yesterdayMetrics.getMa60() == null) {
				DailyStockMetricsDTO dailyStockMetricsDTO = generateRoutineMetricsOldSchool(k, date);
				dailyStockMetricsDTO.setId(todayMetrics.getId());
				todayMetrics = dailyStockMetricsDTO;
			} else {
				metricsCalculator.cal(todayMetrics,
						BeanUtil.copyProperties(v.getFirst(), StockInfo4InitMetricsDTO.class),
						BeanUtil.copyProperties(v.get(1), StockInfo4InitMetricsDTO.class),
						BeanUtil.copyProperties(v.get(2), StockInfo4InitMetricsDTO.class),
						BeanUtil.copyProperties(v.get(3), StockInfo4InitMetricsDTO.class),
						BeanUtil.copyProperties(v.get(4), StockInfo4InitMetricsDTO.class),
//                    BeanUtil.copyProperties(v.get(5), StockInfo4InitMetricsDTO.class),
//                    BeanUtil.copyProperties(v.getLast(), StockInfo4InitMetricsDTO.class),
						yesterdayMetrics);
			}

			results.add(todayMetrics);
		});

		//call feign save
		return remoteStockService.saveMetrics(results, null);
	}

	@Override
	public InnerResponse<ObjectUtils.Null> generateRoutineDetail(Long date) {
		Query4CalMetricsDTO dto = new Query4CalMetricsDTO(date);
		InnerResponse<CalDetailUnionDTO> calDetailInfo = remoteStockService.getCalDetailInfo(dto, null);

		Map<String, List<DailyStockInfoDTO>> stockIdToInfos = calDetailInfo.getData().getStockIdToInfos();
		Map<String, List<DailyStockMetricsDTO>> stockIdToMetrics = calDetailInfo.getData().getStockIdToMetrics();
		Map<String, List<DailyStockInfoDetailDTO>> stockIdToDetails = calDetailInfo.getData().getStockIdToDetails();
		List<DailyStockInfoDetailDTO> results = new ArrayList<>();
		DailyDetailCalculator calculator = new DailyDetailCalculator();

		stockIdToInfos.forEach((k, v) -> {
			v.sort(Comparator.comparingLong(DailyStockInfoDTO::getDate).reversed());
			List<DailyStockMetricsDTO> metrics = stockIdToMetrics.get(k);
			metrics.sort(Comparator.comparingLong(DailyStockMetricsDTO::getDate).reversed());
			List<DailyStockInfoDetailDTO> details = stockIdToDetails.get(k) == null ? Collections.emptyList() : stockIdToDetails.get(k);
			details.sort(Comparator.comparingLong(DailyStockInfoDetailDTO::getDate).reversed());
			DailyStockInfoDetailDTO todayDetail = null;
			DailyStockInfoDetailDTO yesterdayDetail = null;

			//本日有沒有提前按過
			//0 = 沒提前按 沒昨天(今天剛剛上市櫃)
			//1 = 沒提前按 有昨天 or 提前按 沒昨天
			//2 = 提前按 有昨天
			if (details.isEmpty()) {
				todayDetail = new DailyStockInfoDetailDTO();
				todayDetail.setStockId(k);
				todayDetail.setDate(date);
			} else if (details.size() == 1) {
				if (Objects.equals(details.getFirst().getDate(), date)) {
					todayDetail = details.getFirst();
				} else {
					todayDetail = new DailyStockInfoDetailDTO();
					todayDetail.setStockId(k);
					todayDetail.setDate(date);
					yesterdayDetail = details.getFirst();
				}
			} else {
				todayDetail = details.getFirst();
				yesterdayDetail = details.get(1);
			}

			//本日有沒有開市
			if (v.getFirst().getTodayClosingPrice() == null) {
				todayDetail.setUpperShadow(yesterdayDetail.getUpperShadow());
				todayDetail.setRealBody(yesterdayDetail.getRealBody());
				todayDetail.setLowerShadow(yesterdayDetail.getLowerShadow());
				todayDetail.setTags(yesterdayDetail.getTags());
				results.add(todayDetail);
				return;
			} else {
				todayDetail.setTodayClosingPrice(v.getFirst().getTodayClosingPrice());
			}

			DailyStockInfoDTO yesterdayInfo = null;
			try {
				yesterdayInfo = v.get(1);
			} catch (Exception ignored) {
			}

			DailyStockInfoDTO twoDaysAgoInfo = null;
			try {
				twoDaysAgoInfo = v.get(2);
			} catch (Exception ignored) {
			}

			DailyStockInfoDTO fourDaysAgoInfo = null;
			try {
				fourDaysAgoInfo = v.get(3);
			} catch (Exception ignored) {
			}

			DailyStockMetricsDTO todayMetrics = null;
			try {
				todayMetrics = metrics.getFirst();
			} catch (Exception ignored) {
			}

			DailyStockMetricsDTO yesterdayMetrics = null;
			try {
				yesterdayMetrics = metrics.get(1);
			} catch (Exception ignored) {
			}

			calculator.cal(
					todayDetail,
					v.getFirst(),
					yesterdayInfo,
					twoDaysAgoInfo,
					fourDaysAgoInfo,
					todayMetrics,
					yesterdayMetrics,
					yesterdayDetail
			);

			results.add(todayDetail);
		});

		//call feign save
		return remoteStockService.saveDetail(results, null);
	}

	@Override
	public InnerResponse<ObjectUtils.Null> generateRealTimeDetail(Long date) {
		InnerResponse<CalDetailUnionDTO> calDetailInfo = remoteStockService.get4CalRealTimeDetailInfo(date, null);

		Map<String, List<DailyStockInfoDTO>> stockIdToInfos = calDetailInfo.getData().getStockIdToInfos();
		Map<String, List<DailyStockMetricsDTO>> stockIdToMetrics = calDetailInfo.getData().getStockIdToMetrics();
		Map<String, List<DailyStockInfoDetailDTO>> stockIdToDetails = calDetailInfo.getData().getStockIdToDetails();
		List<DailyStockInfoDetailDTO> results = new ArrayList<>();
		DailyDetailCalculator calculator = new DailyDetailCalculator();

		stockIdToInfos.forEach((k, v) -> {
			v.sort(Comparator.comparingLong(DailyStockInfoDTO::getDate).reversed());
			List<DailyStockMetricsDTO> metrics = stockIdToMetrics.get(k);
			metrics.sort(Comparator.comparingLong(DailyStockMetricsDTO::getDate).reversed());
			List<DailyStockInfoDetailDTO> details = stockIdToDetails.get(k) == null ? Collections.emptyList() : stockIdToDetails.get(k);
			details.sort(Comparator.comparingLong(DailyStockInfoDetailDTO::getDate).reversed());
			DailyStockInfoDetailDTO todayDetail;
			DailyStockInfoDetailDTO yesterdayDetail = null;

			//本日有沒有提前按過
			//0 = 沒提前按 沒昨天
			//1 = 沒提前按 有昨天 or 提前按 沒昨天
			//2 = 提前按 有昨天
			if (details.isEmpty()) {
				todayDetail = new DailyStockInfoDetailDTO();
				todayDetail.setStockId(k);
				todayDetail.setDate(date);
			} else if (details.size() == 1) {
				if (Objects.equals(details.getFirst().getDate(), date)) {
					todayDetail = details.getFirst();
				} else {
					todayDetail = new DailyStockInfoDetailDTO();
					todayDetail.setStockId(k);
					todayDetail.setDate(date);
					yesterdayDetail = details.getFirst();
				}
			} else {
				todayDetail = details.getFirst();
				yesterdayDetail = details.get(1);
			}

			//本日有沒有開市
			if (v.getFirst().getTodayClosingPrice() == null) {
				todayDetail.setUpperShadow(yesterdayDetail.getUpperShadow());
				todayDetail.setRealBody(yesterdayDetail.getRealBody());
				todayDetail.setLowerShadow(yesterdayDetail.getLowerShadow());
				todayDetail.setTags(yesterdayDetail.getTags());
				results.add(todayDetail);
				return;
			} else {
				todayDetail.setTodayClosingPrice(v.getFirst().getTodayClosingPrice());
			}

			DailyStockInfoDTO yesterdayInfo = null;
			try {
				yesterdayInfo = v.get(1);
			} catch (Exception ignored) {
			}

			DailyStockInfoDTO twoDaysAgoInfo = null;
			try {
				twoDaysAgoInfo = v.get(2);
			} catch (Exception ignored) {
			}

			DailyStockInfoDTO fourDaysAgoInfo = null;
			try {
				fourDaysAgoInfo = v.get(3);
			} catch (Exception ignored) {
			}

			DailyStockMetricsDTO todayMetrics = null;
			try {
				todayMetrics = metrics.getFirst();
			} catch (Exception ignored) {
			}

			DailyStockMetricsDTO yesterdayMetrics = null;
			try {
				yesterdayMetrics = metrics.get(1);
			} catch (Exception ignored) {
			}

			calculator.cal(
					todayDetail,
					v.getFirst(),
					yesterdayInfo,
					twoDaysAgoInfo,
					fourDaysAgoInfo,
					todayMetrics,
					yesterdayMetrics,
					yesterdayDetail
			);

			results.add(todayDetail);
		});

		//call feign save
		return remoteStockService.saveDetail(results, null);
	}

	@Override
	public DailyStockMetricsDTO generateRoutineMetricsOldSchool(String stockId, Long date) {
		InnerResponse<List<DailyStockInfoDTO>> response = remoteStockService.getInfosByCond(date, stockId);

		List<DailyStockInfoDTO> infos = response.getData();

		DailyStockMetricsDTO dto = new DailyStockMetricsDTO();
		dto.setStockId(stockId);
		dto.setStockName(infos.getFirst().getStockName());
		dto.setDate(date);
		dto.setTodayClosingPrice(infos.getFirst().getTodayClosingPrice());

		try {
			dto.setMa5(
					infos.subList(0, 5)
							.stream()
							.map(DailyStockInfoDTO::getTodayClosingPrice)
							.reduce(BigDecimal.ZERO, BigDecimal::add)
							.divide(BigDecimal.valueOf(5), 2, RoundingMode.FLOOR)
			);
			dto.setLastMA5price(infos.getFirst().getTodayClosingPrice());
		} catch (Exception ignored) {
			return dto;
		}

		try {
			dto.setMa10(
					infos.subList(0, 10)
							.stream()
							.map(DailyStockInfoDTO::getTodayClosingPrice)
							.reduce(BigDecimal.ZERO, BigDecimal::add)
							.divide(BigDecimal.valueOf(10), 2, RoundingMode.FLOOR)
			);
			dto.setLastMA10price(infos.get(9).getTodayClosingPrice());
		} catch (Exception ignored) {
			return dto;
		}

		try {
			dto.setMa20(
					infos.subList(0, 20)
							.stream()
							.map(DailyStockInfoDTO::getTodayClosingPrice)
							.reduce(BigDecimal.ZERO, BigDecimal::add)
							.divide(BigDecimal.valueOf(20), 2, RoundingMode.FLOOR)
			);
			dto.setLastMA20price(infos.get(19).getTodayClosingPrice());
		} catch (Exception ignored) {
			return dto;
		}

		try {
			dto.setMa60(
					infos.subList(0, 60)
							.stream()
							.map(DailyStockInfoDTO::getTodayClosingPrice)
							.reduce(BigDecimal.ZERO, BigDecimal::add)
							.divide(BigDecimal.valueOf(60), 2, RoundingMode.FLOOR)
			);
			dto.setLastMA60price(infos.get(59).getTodayClosingPrice());
		} catch (Exception ignored) {
			return dto;
		}

		return dto;
	}
}
