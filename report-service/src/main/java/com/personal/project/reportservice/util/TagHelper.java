package com.personal.project.reportservice.util;

import com.personal.project.reportservice.constant.DetailTagEnum;
import com.personal.project.reportservice.model.dto.DailyStockInfoDetailDTO;
import com.personal.project.reportservice.model.dto.DailyStockMetricsDTO;
import com.personal.project.reportservice.model.dto.StockInfo4CalDetailDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TagHelper {
    //todo 標籤以多語系方式做英翻中
    public static List<String> generateTags(String pureDate,
                                        DailyStockInfoDetailDTO todayDetailDTO,
                                        StockInfo4CalDetailDTO todayInfo,
                                        StockInfo4CalDetailDTO yesterdayInfo,
                                        List<DailyStockMetricsDTO> dailyStockMetricsDTOs) {

        List<String> tags = new ArrayList<>();

        DailyStockMetricsDTO todayMetrics = dailyStockMetricsDTOs.stream().filter(o -> o.getDate().toString().equals(pureDate)).findFirst().get();
        DailyStockMetricsDTO yesterdayMetrics = dailyStockMetricsDTOs.stream().filter(o -> !o.getDate().toString().equals(pureDate)).findFirst().get();

        generatePriceTags(tags, todayInfo, todayMetrics);
        generateMATags(tags, todayMetrics, yesterdayMetrics);
        generateCrossTags(tags, todayInfo);
        generateLowerShadowTags(tags, todayDetailDTO, todayInfo, todayMetrics);
        generateUpperShadowTags(tags, todayDetailDTO, todayInfo, todayMetrics);
        generateKStickTags(tags, todayInfo, yesterdayInfo, todayDetailDTO);

        return tags;
    }

    private static void generateMATags(List<String> tags, DailyStockMetricsDTO todayMetrics, DailyStockMetricsDTO yesterdayMetrics) {
        if (todayMetrics.getMa5().compareTo(todayMetrics.getMa20()) > 0 && yesterdayMetrics.getMa5().compareTo(yesterdayMetrics.getMa20()) < 0) {
            tags.add(DetailTagEnum.MA5_UP_THROUGH_MA20.getTag());
        }

        if (todayMetrics.getMa20().compareTo(todayMetrics.getMa5()) > 0 && yesterdayMetrics.getMa20().compareTo(yesterdayMetrics.getMa5()) < 0) {
            tags.add(DetailTagEnum.MA20_UP_THROUGH_MA5.getTag());
        }

        if (todayMetrics.getMa20().compareTo(todayMetrics.getMa60()) > 0 && yesterdayMetrics.getMa20().compareTo(yesterdayMetrics.getMa60()) < 0) {
            tags.add(DetailTagEnum.MA20_UP_THROUGH_MA60.getTag());
        }

        if (todayMetrics.getMa5().compareTo(todayMetrics.getMa20()) < 0 && yesterdayMetrics.getMa5().compareTo(yesterdayMetrics.getMa20()) > 0) {
            tags.add(DetailTagEnum.MA5_DOWN_THROUGH_MA20.getTag());
        }

        if (todayMetrics.getMa20().compareTo(todayMetrics.getMa60()) < 0 && yesterdayMetrics.getMa20().compareTo(yesterdayMetrics.getMa60()) > 0) {
            tags.add(DetailTagEnum.MA20_DOWN_THROUGH_MA60.getTag());
        }

        if (todayMetrics.getMa5().compareTo(todayMetrics.getMa20()) > 0 && todayMetrics.getMa20().compareTo(todayMetrics.getMa60()) > 0) {
            tags.add(DetailTagEnum.MA_QUEUED_UP.getTag());
        }
    }

    private static void generatePriceTags(List<String> tags, StockInfo4CalDetailDTO todayInfo, DailyStockMetricsDTO todayMetrics) {
        BigDecimal todayClosingPrice = todayInfo.getTodayClosingPrice();
        BigDecimal ma5 = todayMetrics.getMa5();
        BigDecimal ma20 = todayMetrics.getMa20();
        BigDecimal ma60 = todayMetrics.getMa60();

        if (todayClosingPrice.compareTo(ma5) > 0) {
            tags.add(DetailTagEnum.PRICE_OVER_MA5.getTag());
        }

        if (todayClosingPrice.compareTo(ma20) > 0) {
            tags.add(DetailTagEnum.PRICE_OVER_MA20.getTag());
        }

        if (todayClosingPrice.compareTo(ma60) > 0) {
            tags.add(DetailTagEnum.PRICE_OVER_MA60.getTag());
        }
    }

    private static void generateCrossTags(List<String> tags, StockInfo4CalDetailDTO todayInfo) {
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

        if (todayClosing.compareTo(todayMiddle) == 0) {
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

    private static void generateLowerShadowTags(List<String> tags, DailyStockInfoDetailDTO todayDetailDTO, StockInfo4CalDetailDTO todayInfo, DailyStockMetricsDTO todayMetrics) {
        if (todayDetailDTO.getLowerShadow().compareTo(BigDecimal.ZERO) != 0) {
            tags.add(DetailTagEnum.HAVE_LOWER_SHADOW.getTag());
        } else {
            tags.add(DetailTagEnum.NO_LOWER_SHADOW.getTag());
            return;
        }

        if (todayDetailDTO.getLowerShadow().compareTo(BigDecimal.valueOf(50)) >= 0) {
            tags.add(DetailTagEnum.STRONG_SUPPORT.getTag());
        }

        if (todayInfo.getLowestPrice().compareTo(todayMetrics.getMa5().multiply(BigDecimal.valueOf(0.99))) >= 0 &&
                todayInfo.getLowestPrice().compareTo(todayMetrics.getMa5().multiply(BigDecimal.valueOf(1.01))) <= 0 &&
                todayInfo.getTodayClosingPrice().compareTo(todayMetrics.getMa5()) > 0

        ) {
            tags.add(DetailTagEnum.TESTING_MA5_SUPPORT.getTag());
        }

        if (todayInfo.getLowestPrice().compareTo(todayMetrics.getMa20().multiply(BigDecimal.valueOf(0.99))) >= 0 &&
                todayInfo.getLowestPrice().compareTo(todayMetrics.getMa20().multiply(BigDecimal.valueOf(1.01))) <= 0 &&
                todayInfo.getTodayClosingPrice().compareTo(todayMetrics.getMa20()) > 0
        ) {
            tags.add(DetailTagEnum.TESTING_MA20_SUPPORT.getTag());
        }

        if (todayInfo.getLowestPrice().compareTo(todayMetrics.getMa60().multiply(BigDecimal.valueOf(0.99))) >= 0 &&
                todayInfo.getLowestPrice().compareTo(todayMetrics.getMa60().multiply(BigDecimal.valueOf(1.01))) <= 0 &&
                todayInfo.getTodayClosingPrice().compareTo(todayMetrics.getMa60()) > 0
        ) {
            tags.add(DetailTagEnum.TESTING_MA60_SUPPORT.getTag());
        }

    }

    private static void generateUpperShadowTags(List<String> tags, DailyStockInfoDetailDTO todayDetailDTO, StockInfo4CalDetailDTO todayInfo, DailyStockMetricsDTO todayMetrics) {

        if (todayDetailDTO.getUpperShadow().compareTo(BigDecimal.ZERO) != 0) {
            tags.add(DetailTagEnum.HAVE_UPPER_SHADOW.getTag());
        } else {
            tags.add(DetailTagEnum.NO_UPPER_SHADOW.getTag());
            return;
        }

        if (todayDetailDTO.getUpperShadow().compareTo(BigDecimal.valueOf(50)) >= 0) {
            tags.add(DetailTagEnum.STRONG_PRESSURE.getTag());
        }

        if (todayInfo.getHighestPrice().compareTo(todayMetrics.getMa5().multiply(BigDecimal.valueOf(1.01))) <= 0 &&
                todayInfo.getHighestPrice().compareTo(todayMetrics.getMa5().multiply(BigDecimal.valueOf(0.99))) >= 0 &&
                todayInfo.getTodayClosingPrice().compareTo(todayMetrics.getMa5()) < 0

        ) {
            tags.add(DetailTagEnum.TESTING_MA5_PRESSURE.getTag());
        }

        if (todayInfo.getHighestPrice().compareTo(todayMetrics.getMa20().multiply(BigDecimal.valueOf(1.01))) <= 0 &&
                todayInfo.getHighestPrice().compareTo(todayMetrics.getMa20().multiply(BigDecimal.valueOf(0.99))) >= 0 &&
                todayInfo.getTodayClosingPrice().compareTo(todayMetrics.getMa20()) < 0
        ) {
            tags.add(DetailTagEnum.TESTING_MA20_PRESSURE.getTag());
        }

    }

    private static void generateKStickTags(List<String> tags, StockInfo4CalDetailDTO todayInfo, StockInfo4CalDetailDTO yesterdayInfo, DailyStockInfoDetailDTO todayDetailDTO) {
        BigDecimal todayClosing = todayInfo.getTodayClosingPrice();
        BigDecimal todayOpening = todayInfo.getOpeningPrice();
        BigDecimal todayMiddle = todayOpening.add(todayClosing).divide(BigDecimal.TWO, 2, RoundingMode.FLOOR);
        BigDecimal todayHighest = todayInfo.getHighestPrice();
        BigDecimal todayLowest = todayInfo.getLowestPrice();
        BigDecimal yesterdayClosing = todayInfo.getYesterdayClosingPrice();
        BigDecimal yesterdayOpening = yesterdayInfo.getOpeningPrice();

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

        if (todayOpening.compareTo(todayClosing) < 0) {
            tags.add(DetailTagEnum.RED.getTag());
        }

        if (todayOpening.compareTo(todayClosing) > 0) {
            tags.add(DetailTagEnum.GREEN.getTag());
        }
    }
}
