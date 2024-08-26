package com.personal.project.reportservice.util;

import cn.hutool.core.util.StrUtil;
import com.personal.project.commoncore.constants.CommonTerm;
import com.personal.project.reportservice.constant.DetailTagEnum;
import com.personal.project.reportservice.model.dto.DailyStockInfoDTO;
import com.personal.project.reportservice.model.dto.DailyStockInfoDetailDTO;
import com.personal.project.reportservice.model.dto.DailyStockMetricsDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class DailyDetailCalculator {

	public DailyStockInfoDetailDTO cal(
			DailyStockInfoDetailDTO result,
			DailyStockInfoDTO todayInfo,
			DailyStockInfoDTO yesterdayInfo,
			DailyStockInfoDTO twoDaysAgoInfo,
			DailyStockInfoDTO fourDaysAgoInfo,
			DailyStockMetricsDTO todayMetrics,
			DailyStockMetricsDTO yesterdayMetrics,
			DailyStockInfoDetailDTO yesterdayDetail
	) {
		calShadow(result, todayInfo);

		DailyStockInfoDetailDTO.TagsDTO todayTags = new DailyStockInfoDetailDTO.TagsDTO();

		calConsecutive(todayTags, yesterdayDetail, todayInfo, yesterdayInfo);

		calComparingPassDays(todayTags, todayInfo, twoDaysAgoInfo, fourDaysAgoInfo);

		calExtraTags(result, todayTags, todayInfo, yesterdayInfo, todayMetrics, yesterdayMetrics);

		result.setTags(todayTags);

		return result;
	}

	private void calShadow(DailyStockInfoDetailDTO result, DailyStockInfoDTO todayInfo) {
		BigDecimal range = todayInfo.getHighestPrice().subtract(todayInfo.getLowestPrice());
		if (range.compareTo(BigDecimal.ZERO) != 0) {
			BigDecimal upperShadow = todayInfo.getHighestPrice()
					.subtract(todayInfo.getOpeningPrice().max(todayInfo.getTodayClosingPrice()))
					.divide(range, 4, RoundingMode.FLOOR)
					.multiply(BigDecimal.valueOf(100));
			result.setUpperShadow(upperShadow);

			BigDecimal realBody = todayInfo.getOpeningPrice().subtract(todayInfo.getTodayClosingPrice())
					.abs()
					.divide(range, 4, RoundingMode.FLOOR)
					.multiply(BigDecimal.valueOf(100));
			result.setRealBody(realBody);

			BigDecimal lowerShadow = todayInfo.getOpeningPrice().min(todayInfo.getTodayClosingPrice())
					.subtract(todayInfo.getLowestPrice())
					.divide(range, 4, RoundingMode.FLOOR)
					.multiply(BigDecimal.valueOf(100));
			result.setLowerShadow(lowerShadow);
		} else {
			result.setLowerShadow(BigDecimal.ZERO);
			result.setRealBody(BigDecimal.ZERO);
			result.setUpperShadow(BigDecimal.ZERO);
		}
	}

	private void calConsecutive(DailyStockInfoDetailDTO.TagsDTO todayTags,
								DailyStockInfoDetailDTO yesterdayDetail,
								DailyStockInfoDTO todayInfo,
								DailyStockInfoDTO yesterdayInfo) {
		//正常情況下, yesterdayInfo與yesterdayDetail是同有同無
		if (yesterdayInfo == null) {
			todayTags.setPriceStatus(StrUtil.format("{}->{}", CommonTerm.UNCHANGED, CommonTerm.UNCHANGED));
			//金額連
			todayTags.setConsecutivePrice(new String[]{"1", CommonTerm.UNCHANGED});

			//交易量連
			todayTags.setConsecutiveTradingVolume(new String[]{"1", CommonTerm.UNCHANGED});

			//交易額連
			todayTags.setConsecutiveTradingAmount(new String[]{"1", CommonTerm.UNCHANGED});
		} else {
			String todayPriceStatus = judgeStatus(todayInfo.getPriceGap());
			DailyStockInfoDetailDTO.TagsDTO yesterdayTags = yesterdayDetail.getTags();
			todayTags.setPriceStatus(StrUtil.format("{}->{}", yesterdayTags.getPriceStatus().split("->")[1], todayPriceStatus));

			//金額連
			if (todayPriceStatus.equals(yesterdayTags.getConsecutivePrice()[1])) {
				todayTags.setConsecutivePrice(
						new String[]{
								String.valueOf(Long.parseLong(yesterdayTags.getConsecutivePrice()[0]) + 1),
								todayPriceStatus
						}
				);
			} else {
				todayTags.setConsecutivePrice(
						new String[]{
								"1",
								todayPriceStatus
						}
				);
			}

			//交易量連
			String todayVolumeStatus = judgeStatus(BigDecimal.valueOf(todayInfo.getTodayTradingVolumePiece() - yesterdayInfo.getTodayTradingVolumePiece()));
			if (todayVolumeStatus.equals(yesterdayTags.getConsecutiveTradingVolume()[1])) {
				todayTags.setConsecutiveTradingVolume(
						new String[]{
								String.valueOf(Long.parseLong(yesterdayTags.getConsecutiveTradingVolume()[0]) + 1),
								todayVolumeStatus
						}
				);
			} else {
				todayTags.setConsecutiveTradingVolume(
						new String[]{
								"1",
								todayVolumeStatus
						}
				);
			}

			//交易額連
			if (todayInfo.getTodayTradingVolumeMoney() != null) { //考量到即時報價沒有交易額
				String todayAmountStatus = judgeStatus(todayInfo.getTodayTradingVolumeMoney().subtract(yesterdayInfo.getTodayTradingVolumeMoney()));
				if (todayAmountStatus.equals(yesterdayTags.getConsecutiveTradingAmount()[1])) {
					todayTags.setConsecutiveTradingAmount(
							new String[]{
									String.valueOf(Long.parseLong(yesterdayTags.getConsecutiveTradingAmount()[0]) + 1),
									todayAmountStatus
							}
					);
				} else {
					todayTags.setConsecutiveTradingAmount(
							new String[]{
									"1",
									todayAmountStatus
							}
					);
				}
			}
		}
	}

	private void calComparingPassDays(DailyStockInfoDetailDTO.TagsDTO todayTags, DailyStockInfoDTO todayInfo, DailyStockInfoDTO twoDaysAgoInfo, DailyStockInfoDTO fourDaysAgoInfo) {
		if (twoDaysAgoInfo != null) {
			//前第3天
			BigDecimal vs2DaysPriceDiff = todayInfo.getTodayClosingPrice().subtract(twoDaysAgoInfo.getTodayClosingPrice());
			String vs2DaysPriceStatus = judgeStatus(vs2DaysPriceDiff);
			todayTags.setPriceVS2DaysAgo(
					new String[]{
							vs2DaysPriceStatus,
							vs2DaysPriceDiff.divide(twoDaysAgoInfo.getTodayClosingPrice(), 4, RoundingMode.FLOOR).toPlainString()
					}
			);

			BigDecimal vs2DaysVolumeDiff = BigDecimal.valueOf(todayInfo.getTodayTradingVolumePiece() - twoDaysAgoInfo.getTodayTradingVolumePiece());
			String vs2DaysVolumeStatus = judgeStatus(vs2DaysVolumeDiff);
			todayTags.setTradingVolumeVS2DaysAgo(
					new String[]{
							vs2DaysVolumeStatus,
							vs2DaysVolumeDiff.divide(
									twoDaysAgoInfo.getTodayTradingVolumePiece() == 0 ? BigDecimal.ONE : BigDecimal.valueOf(twoDaysAgoInfo.getTodayTradingVolumePiece()),
									4,
									RoundingMode.FLOOR
							).toPlainString()
					}
			);

			try {//即時報價沒有交易額
				BigDecimal vs2DaysAmountDiff = todayInfo.getTodayTradingVolumeMoney().subtract(twoDaysAgoInfo.getTodayTradingVolumeMoney());
				String vs2DaysAmountStatus = judgeStatus(vs2DaysAmountDiff);
				todayTags.setTradingAmountVS2DaysAgo(
						new String[]{
								vs2DaysAmountStatus,
								vs2DaysAmountDiff.divide(
										twoDaysAgoInfo.getTodayTradingVolumeMoney().compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : twoDaysAgoInfo.getTodayTradingVolumeMoney(),
										4,
										RoundingMode.FLOOR
								).toPlainString()
						}
				);
			} catch (Exception ignored) {
			}

		} else {
			todayTags.setPriceVS2DaysAgo(new String[]{CommonTerm.UNCHANGED, "0"});
			todayTags.setTradingVolumeVS2DaysAgo(new String[]{CommonTerm.UNCHANGED, "0"});
			todayTags.setTradingAmountVS2DaysAgo(new String[]{CommonTerm.UNCHANGED, "0"});
		}

		if (fourDaysAgoInfo != null) {
			//前第5天
			BigDecimal vs4DaysPriceDiff = todayInfo.getTodayClosingPrice().subtract(fourDaysAgoInfo.getTodayClosingPrice());
			String vs4DaysPriceStatus = judgeStatus(vs4DaysPriceDiff);
			todayTags.setPriceVS4DaysAgo(
					new String[]{
							vs4DaysPriceStatus,
							vs4DaysPriceDiff.divide(fourDaysAgoInfo.getTodayClosingPrice(), 4, RoundingMode.FLOOR).toPlainString()
					}
			);

			BigDecimal vs4DaysVolumeDiff = BigDecimal.valueOf(todayInfo.getTodayTradingVolumePiece() - fourDaysAgoInfo.getTodayTradingVolumePiece());
			String vs4DaysVolumeStatus = judgeStatus(vs4DaysVolumeDiff);
			todayTags.setTradingVolumeVS4DaysAgo(
					new String[]{
							vs4DaysVolumeStatus,
							vs4DaysVolumeDiff.divide(
									fourDaysAgoInfo.getTodayTradingVolumePiece() == 0 ? BigDecimal.ONE : BigDecimal.valueOf(fourDaysAgoInfo.getTodayTradingVolumePiece()),
									4,
									RoundingMode.FLOOR
							).toPlainString()
					}
			);

			try {
				BigDecimal vs4DaysAmountDiff = todayInfo.getTodayTradingVolumeMoney().subtract(fourDaysAgoInfo.getTodayTradingVolumeMoney());
				String vs4DaysAmountStatus = judgeStatus(vs4DaysAmountDiff);
				todayTags.setTradingAmountVS4DaysAgo(
						new String[]{
								vs4DaysAmountStatus,
								vs4DaysAmountDiff.divide(
										fourDaysAgoInfo.getTodayTradingVolumeMoney().compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : fourDaysAgoInfo.getTodayTradingVolumeMoney(),
										4,
										RoundingMode.FLOOR
								).toPlainString()
						}
				);
			} catch (Exception ignored) {
			}

		} else {
			todayTags.setPriceVS4DaysAgo(new String[]{CommonTerm.UNCHANGED, "0"});
			todayTags.setTradingVolumeVS4DaysAgo(new String[]{CommonTerm.UNCHANGED, "0"});
			todayTags.setTradingAmountVS4DaysAgo(new String[]{CommonTerm.UNCHANGED, "0"});
		}
	}

	private void calExtraTags(DailyStockInfoDetailDTO result, DailyStockInfoDetailDTO.TagsDTO todayTags, DailyStockInfoDTO todayInfo, DailyStockInfoDTO yesterdayInfo, DailyStockMetricsDTO todayMetrics, DailyStockMetricsDTO yesterdayMetrics) {
		List<String> tags = new ArrayList<>();
		generatePriceTags(tags, todayInfo, todayMetrics);
		generateMATags(tags, todayMetrics, yesterdayMetrics);
		generateCrossTags(tags, todayInfo);
		generateLowerShadowTags(tags, todayInfo, todayMetrics, result);
		generateUpperShadowTags(tags, todayInfo, todayMetrics, result);
		generateKStickTags(tags, todayInfo, yesterdayInfo, result);

		todayTags.setExtraTags(tags);
	}

	private void generateMATags(List<String> tags, DailyStockMetricsDTO todayMetrics, DailyStockMetricsDTO yesterdayMetrics) {
		BigDecimal ma5 = todayMetrics.getMa5();
		BigDecimal yesterdayMa5 = yesterdayMetrics == null ? null : yesterdayMetrics.getMa5();
		BigDecimal ma20 = todayMetrics.getMa20();
		BigDecimal yesterdayMa20 = yesterdayMetrics == null ? null : yesterdayMetrics.getMa20();
		BigDecimal ma60 = todayMetrics.getMa60();
		BigDecimal yesterdayMa60 = yesterdayMetrics == null ? null : yesterdayMetrics.getMa60();

		if (ma5 != null && yesterdayMa5 != null && ma20 != null && yesterdayMa20 != null) {
			if (ma5.compareTo(ma20) > 0 && yesterdayMa5.compareTo(yesterdayMa20) < 0) {
				tags.add(DetailTagEnum.MA5_UP_THROUGH_MA20.getTag());
				tags.add(DetailTagEnum.MA20_DOWN_THROUGH_MA5.getTag());
			}

			if (ma20.compareTo(ma5) > 0 && yesterdayMa20.compareTo(yesterdayMa5) < 0) {
				tags.add(DetailTagEnum.MA20_UP_THROUGH_MA5.getTag());
				tags.add(DetailTagEnum.MA5_DOWN_THROUGH_MA20.getTag());
			}
		}

		if (ma20 != null && ma60 != null && yesterdayMa20 != null && yesterdayMa60 != null) {
			if (ma20.compareTo(ma60) > 0 && yesterdayMa20.compareTo(yesterdayMa60) < 0) {
				tags.add(DetailTagEnum.MA20_UP_THROUGH_MA60.getTag());
				tags.add(DetailTagEnum.MA60_DOWN_THROUGH_MA20.getTag());
			}

			if (ma20.compareTo(ma60) < 0 && yesterdayMa20.compareTo(yesterdayMa60) > 0) {
				tags.add(DetailTagEnum.MA20_DOWN_THROUGH_MA60.getTag());
			}
		}

		if (ma5 != null && ma20 != null && ma60 != null &&
				yesterdayMa5 != null && yesterdayMa20 != null && yesterdayMa60 != null
		) {
			if (ma5.compareTo(ma20) > 0
					&& ma20.compareTo(ma60) > 0
					&& ma5.compareTo(yesterdayMa5) > 0
					&& ma20.compareTo(yesterdayMa20) > 0
					&& ma60.compareTo(yesterdayMa60) > 0
			) {
				tags.add(DetailTagEnum.MA_QUEUED_UP.getTag());
			}
		}
	}

	private void generatePriceTags(List<String> tags, DailyStockInfoDTO todayInfo, DailyStockMetricsDTO todayMetrics) {
		BigDecimal todayClosingPrice = todayInfo.getTodayClosingPrice();
		BigDecimal ma5 = todayMetrics.getMa5();
		BigDecimal ma20 = todayMetrics.getMa20();
		BigDecimal ma60 = todayMetrics.getMa60();

		if (ma5 != null && todayClosingPrice.compareTo(ma5) > 0) {
			tags.add(DetailTagEnum.PRICE_OVER_MA5.getTag());
		}

		if (ma20 != null && todayClosingPrice.compareTo(ma20) > 0) {
			tags.add(DetailTagEnum.PRICE_OVER_MA20.getTag());
		}

		if (ma60 != null && todayClosingPrice.compareTo(ma60) > 0) {
			tags.add(DetailTagEnum.PRICE_OVER_MA60.getTag());
		}
	}

	private void generateCrossTags(List<String> tags, DailyStockInfoDTO todayInfo) {
		BigDecimal todayClosing = todayInfo.getTodayClosingPrice();
		BigDecimal todayOpening = todayInfo.getOpeningPrice();
		boolean crossFlag = todayOpening.compareTo(todayClosing) == 0;
		if (!crossFlag) {
			return;
		}
		BigDecimal todayLowest = todayInfo.getLowestPrice();
		BigDecimal todayHighest = todayInfo.getHighestPrice();
		BigDecimal todayMiddle = todayHighest.add(todayLowest).divide(BigDecimal.TWO, 2, RoundingMode.FLOOR);
		BigDecimal yesterdayClosing = todayInfo.getYesterdayClosingPrice();
		if (todayClosing.compareTo(todayLowest) == 0 &&
				todayLowest.compareTo(todayHighest) == 0 &&
				todayClosing.compareTo(yesterdayClosing) > 0
		) {
			tags.add(DetailTagEnum.JUMP_UP_LIMIT.getTag());
		}

		if (todayClosing.compareTo(todayLowest) == 0 &&
				todayLowest.compareTo(todayHighest) == 0 &&
				todayClosing.compareTo(yesterdayClosing) < 0
		) {
			tags.add(DetailTagEnum.JUMP_DOWM_LIMIT.getTag());
		}

		if (todayClosing.compareTo(todayHighest) == 0 &&
				todayLowest.compareTo(todayHighest) != 0
		) {
			tags.add(DetailTagEnum.T.getTag());
		}

		if (todayClosing.compareTo(todayMiddle.add(todayHighest).divide(BigDecimal.TWO, 2, RoundingMode.FLOOR)) >= 0 &&
				todayClosing.compareTo(todayHighest) < 0
		) {
			tags.add(DetailTagEnum.STRONGER_CROSS.getTag());
		}

		if (todayClosing.compareTo(todayMiddle.add(todayHighest).divide(BigDecimal.TWO, 2, RoundingMode.FLOOR)) < 0 &&
				todayClosing.compareTo(todayMiddle) > 0
		) {
			tags.add(DetailTagEnum.STRONG_CROSS.getTag());
		}

		if (todayClosing.compareTo(todayMiddle) == 0 && todayLowest.compareTo(todayMiddle) != 0 && todayHighest.compareTo(todayMiddle) != 0) {
			tags.add(DetailTagEnum.CROSS.getTag());
		}

		if (todayClosing.compareTo(todayMiddle) < 0 &&
				todayClosing.compareTo(todayMiddle.add(todayLowest).divide(BigDecimal.TWO, 2, RoundingMode.FLOOR)) > 0
		) {
			tags.add(DetailTagEnum.WEAK_CROSS.getTag());
		}

		if (todayClosing.compareTo(todayMiddle) < 0 &&
				todayClosing.compareTo(todayMiddle.add(todayLowest).divide(BigDecimal.TWO, 2, RoundingMode.FLOOR)) <= 0
		) {
			tags.add(DetailTagEnum.WEAKER_CROSS.getTag());
		}

		if (todayClosing.compareTo(todayLowest) == 0 &&
				todayClosing.compareTo(todayHighest) != 0
		) {
			tags.add(DetailTagEnum.GRAVE.getTag());
		}
	}

	private void generateLowerShadowTags(List<String> tags, DailyStockInfoDTO todayInfo, DailyStockMetricsDTO todayMetrics, DailyStockInfoDetailDTO todayDetailDTO) {
		BigDecimal ma5 = todayMetrics.getMa5();
		BigDecimal ma20 = todayMetrics.getMa20();
		BigDecimal ma60 = todayMetrics.getMa60();

		if (todayDetailDTO.getLowerShadow().compareTo(BigDecimal.ZERO) != 0) {
			tags.add(DetailTagEnum.HAVE_LOWER_SHADOW.getTag());
		} else {
			tags.add(DetailTagEnum.NO_LOWER_SHADOW.getTag());
			return;
		}

		if (todayDetailDTO.getLowerShadow().compareTo(BigDecimal.valueOf(50)) >= 0) {
			tags.add(DetailTagEnum.STRONG_SUPPORT.getTag());
		}

		if (ma5 != null &&
				todayInfo.getLowestPrice().compareTo(ma5.multiply(BigDecimal.valueOf(0.99))) >= 0 &&
				todayInfo.getLowestPrice().compareTo(ma5.multiply(BigDecimal.valueOf(1.01))) <= 0 &&
				todayInfo.getTodayClosingPrice().compareTo(ma5) > 0

		) {
			tags.add(DetailTagEnum.TESTING_MA5_SUPPORT.getTag());
		}

		if (ma20 != null &&
				todayInfo.getLowestPrice().compareTo(ma20.multiply(BigDecimal.valueOf(0.99))) >= 0 &&
				todayInfo.getLowestPrice().compareTo(ma20.multiply(BigDecimal.valueOf(1.01))) <= 0 &&
				todayInfo.getTodayClosingPrice().compareTo(ma20) > 0
		) {
			tags.add(DetailTagEnum.TESTING_MA20_SUPPORT.getTag());
		}

		if (ma60 != null &&
				todayInfo.getLowestPrice().compareTo(ma60.multiply(BigDecimal.valueOf(0.99))) >= 0 &&
				todayInfo.getLowestPrice().compareTo(ma60.multiply(BigDecimal.valueOf(1.01))) <= 0 &&
				todayInfo.getTodayClosingPrice().compareTo(ma60) > 0
		) {
			tags.add(DetailTagEnum.TESTING_MA60_SUPPORT.getTag());
		}
	}

	private void generateUpperShadowTags(List<String> tags, DailyStockInfoDTO todayInfo, DailyStockMetricsDTO todayMetrics, DailyStockInfoDetailDTO todayDetailDTO) {
		BigDecimal ma5 = todayMetrics.getMa5();
		BigDecimal ma20 = todayMetrics.getMa20();

		if (todayDetailDTO.getUpperShadow().compareTo(BigDecimal.ZERO) != 0) {
			tags.add(DetailTagEnum.HAVE_UPPER_SHADOW.getTag());
		} else {
			tags.add(DetailTagEnum.NO_UPPER_SHADOW.getTag());
			return;
		}

		if (todayDetailDTO.getUpperShadow().compareTo(BigDecimal.valueOf(50)) >= 0) {
			tags.add(DetailTagEnum.STRONG_PRESSURE.getTag());
		}

		if (ma5 != null &&
				todayInfo.getHighestPrice().compareTo(ma5.multiply(BigDecimal.valueOf(1.01))) <= 0 &&
				todayInfo.getHighestPrice().compareTo(ma5.multiply(BigDecimal.valueOf(0.99))) >= 0 &&
				todayInfo.getTodayClosingPrice().compareTo(ma5) < 0

		) {
			tags.add(DetailTagEnum.TESTING_MA5_PRESSURE.getTag());
		}

		if (ma20 != null &&
				todayInfo.getHighestPrice().compareTo(ma20.multiply(BigDecimal.valueOf(1.01))) <= 0 &&
				todayInfo.getHighestPrice().compareTo(ma20.multiply(BigDecimal.valueOf(0.99))) >= 0 &&
				todayInfo.getTodayClosingPrice().compareTo(ma20) < 0
		) {
			tags.add(DetailTagEnum.TESTING_MA20_PRESSURE.getTag());
		}
	}

	private void generateKStickTags(List<String> tags, DailyStockInfoDTO todayInfo, DailyStockInfoDTO yesterdayInfo, DailyStockInfoDetailDTO todayDetailDTO) {
		BigDecimal todayClosing = null;
		BigDecimal todayOpening = null;
		BigDecimal todayMiddle = null;
		BigDecimal todayHighest = null;
		BigDecimal todayLowest = null;
		if (todayInfo.getTodayClosingPrice() != null) {
			todayClosing = todayInfo.getTodayClosingPrice();
			todayOpening = todayInfo.getOpeningPrice();
			todayMiddle = todayOpening.add(todayClosing).divide(BigDecimal.TWO, 2, RoundingMode.FLOOR);
			todayHighest = todayInfo.getHighestPrice();
			todayLowest = todayInfo.getLowestPrice();
		}

		BigDecimal yesterdayClosing = null;
		BigDecimal yesterdayOpening = null;
		if (yesterdayInfo != null) {
			yesterdayClosing = yesterdayInfo.getTodayClosingPrice();
			yesterdayOpening = yesterdayInfo.getOpeningPrice();
		}

		if (todayClosing != null) {
			if (todayClosing.compareTo(todayOpening) > 0 &&
					todayClosing.compareTo(todayMiddle.add(todayHighest).divide(BigDecimal.TWO, 2, RoundingMode.FLOOR)) >= 0 &&
					todayOpening.compareTo(todayMiddle.add(todayHighest).divide(BigDecimal.TWO, 2, RoundingMode.FLOOR)) >= 0
			) {
				tags.add(DetailTagEnum.RED_HAMMER.getTag());
			}

			if (todayClosing.compareTo(todayOpening) < 0 &&
					todayClosing.compareTo(todayMiddle.add(todayHighest).divide(BigDecimal.TWO, 2, RoundingMode.FLOOR)) >= 0 &&
					todayOpening.compareTo(todayMiddle.add(todayHighest).divide(BigDecimal.TWO, 2, RoundingMode.FLOOR)) >= 0
			) {
				tags.add(DetailTagEnum.GREEN_HAMMER.getTag());
			}

			if (todayClosing.compareTo(todayOpening) > 0 &&
					todayDetailDTO.getRealBody().compareTo(BigDecimal.valueOf(50)) > 0
			) {
				tags.add(DetailTagEnum.LONG_RED.getTag());
			}

			if (todayDetailDTO.getRealBody().compareTo(BigDecimal.valueOf(100L)) == 0 &&
					todayClosing.compareTo(todayOpening) > 0
			) {
				tags.add(DetailTagEnum.MAX_RED.getTag());
			}

			if (todayDetailDTO.getRealBody().compareTo(BigDecimal.valueOf(100L)) == 0 &&
					todayClosing.compareTo(todayOpening) < 0
			) {
				tags.add(DetailTagEnum.MAX_GREEN.getTag());
			}

			if (todayClosing.compareTo(todayOpening) < 0 &&
					todayDetailDTO.getRealBody().compareTo(BigDecimal.valueOf(50)) > 0
			) {
				tags.add(DetailTagEnum.LONG_GREEN.getTag());
			}

			if (todayClosing.compareTo(todayOpening) > 0 &&
					todayClosing.compareTo(todayLowest.add(todayMiddle).divide(BigDecimal.TWO, 2, RoundingMode.FLOOR)) < 0 &&
					todayOpening.compareTo(todayLowest.add(todayMiddle).divide(BigDecimal.TWO, 2, RoundingMode.FLOOR)) < 0
			) {
				tags.add(DetailTagEnum.RED_INVERTED_HAMMER.getTag());
			}

			if (todayClosing.compareTo(todayOpening) < 0 &&
					todayClosing.compareTo(todayLowest.add(todayMiddle).divide(BigDecimal.TWO, 2, RoundingMode.FLOOR)) < 0 &&
					todayOpening.compareTo(todayLowest.add(todayMiddle).divide(BigDecimal.TWO, 2, RoundingMode.FLOOR)) < 0) {
				tags.add(DetailTagEnum.GREEN_INVERTED_HAMMER.getTag());
			}

			if (todayOpening.compareTo(todayClosing) < 0) {
				tags.add(DetailTagEnum.RED.getTag());
			}

			if (todayOpening.compareTo(todayClosing) > 0) {
				tags.add(DetailTagEnum.GREEN.getTag());
			}

			if (yesterdayClosing != null) {
				if (todayOpening.compareTo(yesterdayClosing) > 0 &&
						todayClosing.compareTo(yesterdayClosing) > 0
				) {
					tags.add(DetailTagEnum.GAP_UP.getTag());
				}

				if (todayOpening.compareTo(yesterdayClosing.min(yesterdayOpening)) < 0 &&
						todayClosing.compareTo(yesterdayClosing.min(yesterdayOpening)) < 0
				) {
					tags.add(DetailTagEnum.GAP_DOWN.getTag());
				}
			}

			if (yesterdayOpening != null && yesterdayClosing != null) {
				if (
						todayClosing.compareTo(todayOpening) < 0 &&
								yesterdayClosing.compareTo(yesterdayOpening) > 0 &&
								todayOpening.compareTo(yesterdayClosing) > 0 &&
								todayClosing.compareTo(yesterdayOpening) < 0
				) {
					tags.add(DetailTagEnum.GREEN_COVER.getTag());
				}

				if (
						todayClosing.compareTo(todayOpening) > 0 &&
								yesterdayClosing.compareTo(yesterdayOpening) < 0 &&
								todayClosing.compareTo(yesterdayOpening) > 0 &&
								todayOpening.compareTo(yesterdayClosing) < 0
				) {
					tags.add(DetailTagEnum.RED_COVER.getTag());
				}

				if (
						todayClosing.compareTo(todayOpening) > 0 &&
								yesterdayClosing.compareTo(yesterdayOpening) < 0 &&
								todayClosing.compareTo(yesterdayOpening) < 0 &&
								todayOpening.compareTo(yesterdayClosing) > 0
				) {
					tags.add(DetailTagEnum.PREGNANT.getTag());
				}
			}
		}
	}

	private String judgeStatus(BigDecimal bigDecimal) {

		if (bigDecimal.compareTo(BigDecimal.ZERO) > 0) {
			return CommonTerm.RISE;
		} else if (bigDecimal.compareTo(BigDecimal.ZERO) < 0) {
			return CommonTerm.FALL;
		} else {
			return CommonTerm.UNCHANGED;
		}
	}
}
