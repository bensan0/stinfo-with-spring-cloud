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
        calConsecutive(result, yesterdayDetail, todayInfo, yesterdayInfo);
        calComparingPassDays(result, todayInfo, twoDaysAgoInfo, fourDaysAgoInfo);
        calExtraTags(result, todayInfo, yesterdayInfo, todayMetrics, yesterdayMetrics);

        return result;
    }

    private void calShadow(DailyStockInfoDetailDTO result, DailyStockInfoDTO todayInfo) {
        BigDecimal range = todayInfo.getHighestPrice().subtract(todayInfo.getLowestPrice());

        BigDecimal upperShadow = todayInfo.getHighestPrice()
                .subtract(todayInfo.getOpeningPrice().max(todayInfo.getOpeningPrice()))
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

    }

    private void calConsecutive(DailyStockInfoDetailDTO result, DailyStockInfoDetailDTO yesterdayDetail, DailyStockInfoDTO todayInfo, DailyStockInfoDTO yesterdayInfo) {
        DailyStockInfoDetailDTO.TagsDTO yesterdayTags = yesterdayDetail.getTags();
        DailyStockInfoDetailDTO.TagsDTO todayTags = result.getTags();

        String todayPriceStatus = judgeStatus(todayInfo.getPriceGap());
        todayTags.setPriceStatus(StrUtil.format("{}->{}", yesterdayTags.getPriceStatus().split("->")[1], todayPriceStatus));

        //金額連
        if (todayPriceStatus.equals(yesterdayTags.getConsecutivePrice()[1])) {
            todayTags.setConsecutivePrice(
                    new String[]{
                            String.valueOf(Long.parseLong(yesterdayTags.getConsecutivePrice()[1]) + 1),
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
        String todayAmountStatus = judgeStatus(todayInfo.getTodayTradingVolumeMoney().subtract(yesterdayInfo.getTodayTradingVolumeMoney()));
        if (todayAmountStatus.equals(yesterdayTags.getConsecutiveTradingAmount()[1])) {
            todayTags.setConsecutiveTradingVolume(
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


        result.setTags(todayTags);
    }

    private void calComparingPassDays(DailyStockInfoDetailDTO result, DailyStockInfoDTO todayInfo, DailyStockInfoDTO twoDaysAgoInfo, DailyStockInfoDTO fourDaysAgoInfo) {
        DailyStockInfoDetailDTO.TagsDTO todayTags = result.getTags();

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
                        vs2DaysVolumeDiff.divide(BigDecimal.valueOf(twoDaysAgoInfo.getTodayTradingVolumePiece()), 4, RoundingMode.FLOOR).toPlainString()
                }
        );

        BigDecimal vs2DaysAmountDiff = todayInfo.getTodayTradingVolumeMoney().subtract(twoDaysAgoInfo.getTodayTradingVolumeMoney());
        String vs2DaysAmountStatus = judgeStatus(vs2DaysAmountDiff);
        todayTags.setTradingAmountVS2DaysAgo(
                new String[]{
                        vs2DaysAmountStatus,
                        vs2DaysAmountDiff.divide(twoDaysAgoInfo.getTodayTradingVolumeMoney(), 4, RoundingMode.FLOOR).toPlainString()
                }
        );

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
                        vs4DaysVolumeDiff.divide(BigDecimal.valueOf(fourDaysAgoInfo.getTodayTradingVolumePiece()), 4, RoundingMode.FLOOR).toPlainString()
                }
        );

        BigDecimal vs4DaysAmountDiff = todayInfo.getTodayTradingVolumeMoney().subtract(fourDaysAgoInfo.getTodayTradingVolumeMoney());
        String vs4DaysAmountStatus = judgeStatus(vs4DaysAmountDiff);
        todayTags.setTradingAmountVS4DaysAgo(
                new String[]{
                        vs4DaysAmountStatus,
                        vs4DaysAmountDiff.divide(fourDaysAgoInfo.getTodayTradingVolumeMoney(), 4, RoundingMode.FLOOR).toPlainString()
                }
        );

        result.setTags(todayTags);
    }

    private void calExtraTags(DailyStockInfoDetailDTO result, DailyStockInfoDTO todayInfo, DailyStockInfoDTO yesterdayInfo, DailyStockMetricsDTO todayMetrics, DailyStockMetricsDTO yesterdayMetrics) {
        List<String> tags = new ArrayList<>();
        generatePriceTags(tags, todayInfo, todayMetrics);
        generateMATags(tags, todayMetrics, yesterdayMetrics);
        generateCrossTags(tags, todayInfo);
        generateLowerShadowTags(tags, todayInfo, todayMetrics, result);
        generateUpperShadowTags(tags, todayInfo, todayMetrics, result);
        generateKStickTags(tags, todayInfo, yesterdayInfo, result);

        result.getTags().setExtraTags(tags);
    }

    private void generateMATags(List<String> tags, DailyStockMetricsDTO todayMetrics, DailyStockMetricsDTO yesterdayMetrics) {
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

    private void generatePriceTags(List<String> tags, DailyStockInfoDTO todayInfo, DailyStockMetricsDTO todayMetrics) {
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

    private void generateLowerShadowTags(List<String> tags, DailyStockInfoDTO todayInfo, DailyStockMetricsDTO todayMetrics, DailyStockInfoDetailDTO todayDetailDTO) {
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

    private void generateUpperShadowTags(List<String> tags, DailyStockInfoDTO todayInfo, DailyStockMetricsDTO todayMetrics, DailyStockInfoDetailDTO todayDetailDTO) {

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

    private void generateKStickTags(List<String> tags, DailyStockInfoDTO todayInfo, DailyStockInfoDTO yesterdayInfo, DailyStockInfoDetailDTO todayDetailDTO) {
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
