package com.personal.project.reportservice.util;

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
//			DailyStockInfoDTO twoDaysAgoInfo,
//			DailyStockInfoDTO fourDaysAgoInfo,
			DailyStockMetricsDTO todayMetrics,
			DailyStockMetricsDTO yesterdayMetrics
//			DailyStockInfoDetailDTO yesterdayDetail
	) {
		calShadow(result, todayInfo);

		DailyStockInfoDetailDTO.TagsDTO todayTags = new DailyStockInfoDetailDTO.TagsDTO();

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

	private void calExtraTags(DailyStockInfoDetailDTO result, DailyStockInfoDetailDTO.TagsDTO todayTags, DailyStockInfoDTO todayInfo, DailyStockInfoDTO yesterdayInfo, DailyStockMetricsDTO todayMetrics, DailyStockMetricsDTO yesterdayMetrics) {
		List<String> tags = new ArrayList<>();
		generatePriceTags(tags, todayInfo, yesterdayInfo, todayMetrics);
		generateMATags(tags, todayMetrics, yesterdayMetrics);
		generateCrossTags(tags, todayInfo);
		generateLowerShadowTags(tags, todayInfo, todayMetrics, result);
		generateUpperShadowTags(tags, todayInfo, todayMetrics, result);
		generateKStickTags(tags, todayInfo, yesterdayInfo, result);
		generatePieceTags(tags, todayInfo, yesterdayInfo);

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

	private void generatePriceTags(List<String> tags, DailyStockInfoDTO todayInfo, DailyStockInfoDTO yesterdayInfo, DailyStockMetricsDTO todayMetrics) {
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

		if (todayClosingPrice != null && yesterdayInfo.getTodayClosingPrice() != null) {
			if (todayClosingPrice.compareTo(yesterdayInfo.getTodayClosingPrice()) > 0) {
				tags.add(DetailTagEnum.PRICE_RISE.getTag());
			} else if (todayClosingPrice.compareTo(yesterdayInfo.getTodayClosingPrice()) < 0) {
				tags.add(DetailTagEnum.PRICE_FALL.getTag());
			}
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

	private void generatePieceTags(List<String> tags, DailyStockInfoDTO todayInfo, DailyStockInfoDTO yesterdayInfo) {
		if (todayInfo.getTodayTradingVolumePiece() != null && yesterdayInfo.getTodayTradingVolumePiece() != null
				&& todayInfo.getTodayTradingVolumePiece() != 0 && yesterdayInfo.getTodayTradingVolumePiece() != 0) {
			if (todayInfo.getTodayTradingVolumePiece() / yesterdayInfo.getTodayTradingVolumePiece() >= 2) {
				tags.add(DetailTagEnum.PLENTY_PIECE.getTag());
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
