package com.personal.project.reportservice.util;

import com.personal.project.reportservice.model.dto.DailyStockMetricsDTO;
import com.personal.project.reportservice.model.dto.StockInfo4InitMetricsDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DailyMetricsCalculator {

    //MA計算：{前一個交易日metric.maX * X + (最新info.closingPrice - 前一個交易日metric.maX.lastPrice)}/X
    public void cal(
            DailyStockMetricsDTO result,
            StockInfo4InitMetricsDTO todayInfo,
            StockInfo4InitMetricsDTO ago4DaysInfo,
            StockInfo4InitMetricsDTO ago9DaysInfo,
            StockInfo4InitMetricsDTO ago19DaysInfo,
            StockInfo4InitMetricsDTO ago59DaysInfo,
            StockInfo4InitMetricsDTO ago119DaysInfo,
            StockInfo4InitMetricsDTO ago239DaysInfo,
            DailyStockMetricsDTO yesterdayMetrics
    ) {
        calMA5(result, todayInfo, ago4DaysInfo, yesterdayMetrics);
        calMA10(result, todayInfo, ago9DaysInfo, yesterdayMetrics);
        calMA20(result, todayInfo, ago19DaysInfo, yesterdayMetrics);
        calMA60(result, todayInfo, ago59DaysInfo, yesterdayMetrics);
        calMA120(result, todayInfo, ago119DaysInfo, yesterdayMetrics);
        calMA240(result, todayInfo, ago239DaysInfo, yesterdayMetrics);
    }

    private void calMA5(DailyStockMetricsDTO result, StockInfo4InitMetricsDTO todayInfo, StockInfo4InitMetricsDTO ago4DaysInfo, DailyStockMetricsDTO yesterdayMetrics) {
        BigDecimal ma5 = yesterdayMetrics.getMa5().multiply(BigDecimal.valueOf(5))
                .add(todayInfo.getTodayClosingPrice())
                .subtract(yesterdayMetrics.getLastMA5price())
                .divide(BigDecimal.valueOf(5), 4, RoundingMode.FLOOR);
        result.setMa5(ma5);
        result.setLastMA5price(ago4DaysInfo.getTodayClosingPrice());
    }

    private void calMA10(DailyStockMetricsDTO result, StockInfo4InitMetricsDTO todayInfo, StockInfo4InitMetricsDTO ago9DaysInfo, DailyStockMetricsDTO yesterdayMetrics) {
        BigDecimal ma10 = yesterdayMetrics.getMa10().multiply(BigDecimal.valueOf(10))
                .add(todayInfo.getTodayClosingPrice())
                .subtract(yesterdayMetrics.getLastMA10price())
                .divide(BigDecimal.valueOf(10), 4, RoundingMode.FLOOR);
        result.setMa10(ma10);
        result.setLastMA10price(ago9DaysInfo.getTodayClosingPrice());
    }

    private void calMA20(DailyStockMetricsDTO result, StockInfo4InitMetricsDTO todayInfo, StockInfo4InitMetricsDTO ago19DaysInfo, DailyStockMetricsDTO yesterdayMetrics) {
        BigDecimal ma20 = yesterdayMetrics.getMa20().multiply(BigDecimal.valueOf(20))
                .add(todayInfo.getTodayClosingPrice())
                .subtract(yesterdayMetrics.getLastMA20price())
                .divide(BigDecimal.valueOf(20), 4, RoundingMode.FLOOR);
        result.setMa20(ma20);
        result.setLastMA20price(ago19DaysInfo.getTodayClosingPrice());
    }

    private void calMA60(DailyStockMetricsDTO result, StockInfo4InitMetricsDTO todayInfo, StockInfo4InitMetricsDTO ago59DaysInfo, DailyStockMetricsDTO yesterdayMetrics) {
        BigDecimal ma60 = yesterdayMetrics.getMa60().multiply(BigDecimal.valueOf(60))
                .add(todayInfo.getTodayClosingPrice())
                .subtract(yesterdayMetrics.getLastMA60price())
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.FLOOR);
        result.setMa60(ma60);
        result.setLastMA60price(ago59DaysInfo.getTodayClosingPrice());
    }

    private void calMA120(DailyStockMetricsDTO result, StockInfo4InitMetricsDTO todayInfo, StockInfo4InitMetricsDTO ago119DaysInfo, DailyStockMetricsDTO yesterdayMetrics) {
        BigDecimal ma120 = yesterdayMetrics.getMa120().multiply(BigDecimal.valueOf(120))
                .add(todayInfo.getTodayClosingPrice())
                .subtract(yesterdayMetrics.getLastMA120price())
                .divide(BigDecimal.valueOf(120), 4, RoundingMode.FLOOR);
        result.setMa120(ma120);
        result.setLastMA120price(ago119DaysInfo.getTodayClosingPrice());
    }

    private void calMA240(DailyStockMetricsDTO result, StockInfo4InitMetricsDTO todayInfo, StockInfo4InitMetricsDTO ago239DaysInfo, DailyStockMetricsDTO yesterdayMetrics) {
        BigDecimal ma240 = yesterdayMetrics.getMa240().multiply(BigDecimal.valueOf(240))
                .add(todayInfo.getTodayClosingPrice())
                .subtract(yesterdayMetrics.getLastMA240price())
                .divide(BigDecimal.valueOf(240), 4, RoundingMode.FLOOR);
        result.setMa240(ma240);
        result.setLastMA240price(ago239DaysInfo.getTodayClosingPrice());
    }
}
