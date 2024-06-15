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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
            //第一個是最新交易日資料
            //產出上個交易日指標
            v.sort(Comparator.comparingLong(StockInfo4InitMetricsDTO::getDate).reversed());
            BigDecimal ma5 = v.subList(1, 6).stream()
                    .map(StockInfo4InitMetricsDTO::getTodayClosingPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(5), 2, RoundingMode.FLOOR);
            BigDecimal lastMA5Price = v.get(5).getTodayClosingPrice();

            BigDecimal ma10 = v.subList(1, 11).stream()
                    .map(StockInfo4InitMetricsDTO::getTodayClosingPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(10), 2, RoundingMode.FLOOR);
            BigDecimal lastMA10Price = v.get(10).getTodayClosingPrice();

            BigDecimal ma20 = v.subList(1, 21).stream()
                    .map(StockInfo4InitMetricsDTO::getTodayClosingPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(20), 2, RoundingMode.FLOOR);
            BigDecimal lastMA20Price = v.get(20).getTodayClosingPrice();

            BigDecimal ma60 = v.subList(1, 61).stream()
                    .map(StockInfo4InitMetricsDTO::getTodayClosingPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.FLOOR);
            BigDecimal lastMA60Price = v.get(60).getTodayClosingPrice();

            BigDecimal ma120 = v.subList(1, 121).stream()
                    .map(StockInfo4InitMetricsDTO::getTodayClosingPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(120), 2, RoundingMode.FLOOR);
            BigDecimal lastMA120Price = v.get(120).getTodayClosingPrice();

            BigDecimal ma240 = v.subList(1, 241).stream()
                    .map(StockInfo4InitMetricsDTO::getTodayClosingPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(240), 2, RoundingMode.FLOOR);
            BigDecimal lastMA240Price = v.get(240).getTodayClosingPrice();

            DailyStockMetricsDTO dto = DailyStockMetricsDTO.builder()
                    .stockId(k)
                    .stockName(v.getFirst().getStockName())
                    .date(v.getFirst().getDate())
                    .todayClosingPrice(v.get(1).getTodayClosingPrice())
                    .ma5(ma5)
                    .lastMA5price(lastMA5Price)
                    .ma10(ma10)
                    .lastMA10price(lastMA10Price)
                    .ma20(ma20)
                    .lastMA20price(lastMA20Price)
                    .ma60(ma60)
                    .lastMA60price(lastMA60Price)
                    .ma120(ma120)
                    .lastMA120price(lastMA120Price)
                    .ma240(ma240)
                    .lastMA240price(lastMA240Price)
                    .build();

            //上一個交易日與最新交易日之間若非隔日, 則填補兩日之間非週六週日的資料
            LocalDate newestTradingDate = LocalDate.parse(v.getFirst().getDate().toString(), DatePattern.PURE_DATE_FORMATTER);
            LocalDate lastTradingDate = LocalDate.parse(v.get(1).getDate().toString(), DatePattern.PURE_DATE_FORMATTER);

            if (!newestTradingDate.minusDays(1).isEqual(lastTradingDate)) {
                LocalDate tempDate = lastTradingDate.plusDays(1);
                while (!tempDate.isEqual(newestTradingDate) && tempDate.isBefore(newestTradingDate)) {
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
        //最新交易日info +不含最新交易日 11個交易日份info(sequence 1~12)
        InnerResponse<Map<String, List<StockInfo4InitDetailDTO>>> response = remoteStockService.get4CalInitYesterdayDetail(null);
        Map<String, List<StockInfo4InitDetailDTO>> stockIdToInfoDTOs = response.getData();
        List<DailyStockInfoDetailDTO> result = new ArrayList<>();

        stockIdToInfoDTOs.forEach((k, v) -> {
            v.sort(Comparator.comparingLong(StockInfo4InitDetailDTO::getDate).reversed());
            //第二筆是上個交易日資料
            StockInfo4InitDetailDTO second = v.get(1);
            DailyStockInfoDetailDTO detail = new DailyStockInfoDetailDTO();
            detail.setStockId(k);
            detail.setDate(second.getDate());
            detail.setTodayClosingPrice(second.getTodayClosingPrice());

            //影線與實體計算
            BigDecimal range = second.getHighestPrice().subtract(second.getLowestPrice());

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
                    tempConsecutiveTradingAmount[0] = String.valueOf(Integer.parseInt(tempConsecutiveTradingAmount[0] + 1));
                } else {
                    tempConsecutiveTradingAmount[0] = "1";
                    tempConsecutiveTradingAmount[1] = st;
                }

                tempPrice = dto.getTodayClosingPrice();
                tempVolume = dto.getTodayTradingVolumePiece();
                tempAmount = dto.getTodayTradingVolumeMoney();
            }

            detail.getTags().setConsecutivePrice(tempConsecutivePrice);
            detail.getTags().setConsecutiveTradingVolume(tempConsecutiveTradingVolume);
            detail.getTags().setConsecutiveTradingAmount(tempConsecutiveTradingAmount);
            detail.getTags().setPriceStatus(StrUtil.format("{}->{}", priceStatus[0], priceStatus[1]));

            //對比三天前
            BigDecimal vs2daysPriceDiff = second.getTodayClosingPrice()
                    .subtract(v.get(3).getTodayClosingPrice())
                    .divide(v.get(3).getTodayClosingPrice(), 4, RoundingMode.FLOOR)
                    .multiply(BigDecimal.valueOf(100));
            if (vs2daysPriceDiff.compareTo(BigDecimal.ZERO) > 0) {
                detail.getTags().setPriceVS2DaysAgo(new String[]{CommonTerm.RISE, vs2daysPriceDiff.abs().toPlainString()});
            } else if (vs2daysPriceDiff.compareTo(BigDecimal.ZERO) < 0) {
                detail.getTags().setPriceVS2DaysAgo(new String[]{CommonTerm.FALL, vs2daysPriceDiff.abs().toPlainString()});
            } else {
                detail.getTags().setPriceVS2DaysAgo(new String[]{CommonTerm.UNCHANGED, vs2daysPriceDiff.abs().toPlainString()});
            }
            BigDecimal vs2daysVolumeDiff = second.getTodayTradingVolumePiece()
                    .subtract(v.get(3).getTodayTradingVolumePiece())
                    .divide(v.get(3).getTodayTradingVolumePiece(), 4, RoundingMode.FLOOR)
                    .multiply(BigDecimal.valueOf(100));
            if (vs2daysVolumeDiff.compareTo(BigDecimal.ZERO) > 0) {
                detail.getTags().setTradingVolumeVS2DaysAgo(new String[]{CommonTerm.RISE, vs2daysVolumeDiff.abs().toPlainString()});
            } else if (vs2daysVolumeDiff.compareTo(BigDecimal.ZERO) < 0) {
                detail.getTags().setTradingVolumeVS2DaysAgo(new String[]{CommonTerm.FALL, vs2daysVolumeDiff.abs().toPlainString()});
            } else {
                detail.getTags().setTradingVolumeVS2DaysAgo(new String[]{CommonTerm.UNCHANGED, vs2daysVolumeDiff.abs().toPlainString()});
            }
            BigDecimal vs2daysAmountDiff = second.getTodayTradingVolumeMoney()
                    .subtract(v.get(3).getTodayTradingVolumeMoney())
                    .divide(v.get(3).getTodayTradingVolumeMoney(), 4, RoundingMode.FLOOR)
                    .multiply(BigDecimal.valueOf(100));
            if (vs2daysAmountDiff.compareTo(BigDecimal.ZERO) > 0) {
                detail.getTags().setTradingAmountVS2DaysAgo(new String[]{CommonTerm.RISE, vs2daysAmountDiff.abs().toPlainString()});
            } else if (vs2daysAmountDiff.compareTo(BigDecimal.ZERO) < 0) {
                detail.getTags().setTradingAmountVS2DaysAgo(new String[]{CommonTerm.FALL, vs2daysAmountDiff.abs().toPlainString()});
            } else {
                detail.getTags().setTradingAmountVS2DaysAgo(new String[]{CommonTerm.UNCHANGED, vs2daysAmountDiff.abs().toPlainString()});
            }

            //對比五天前
            BigDecimal vs4daysPriceDiff = second.getTodayClosingPrice()
                    .subtract(v.get(5).getTodayClosingPrice())
                    .divide(v.get(5).getTodayClosingPrice(), 4, RoundingMode.FLOOR)
                    .multiply(BigDecimal.valueOf(100));
            if (vs4daysPriceDiff.compareTo(BigDecimal.ZERO) > 0) {
                detail.getTags().setPriceVS4DaysAgo(new String[]{CommonTerm.RISE, vs4daysPriceDiff.abs().toPlainString()});
            } else if (vs4daysPriceDiff.compareTo(BigDecimal.ZERO) < 0) {
                detail.getTags().setPriceVS4DaysAgo(new String[]{CommonTerm.FALL, vs4daysPriceDiff.abs().toPlainString()});
            } else {
                detail.getTags().setPriceVS4DaysAgo(new String[]{CommonTerm.UNCHANGED, vs4daysPriceDiff.abs().toPlainString()});
            }
            BigDecimal vs4daysVolumeDiff = second.getTodayTradingVolumePiece()
                    .subtract(v.get(5).getTodayTradingVolumePiece())
                    .divide(v.get(5).getTodayTradingVolumePiece(), 4, RoundingMode.FLOOR)
                    .multiply(BigDecimal.valueOf(100));
            if (vs4daysVolumeDiff.compareTo(BigDecimal.ZERO) > 0) {
                detail.getTags().setTradingVolumeVS4DaysAgo(new String[]{CommonTerm.RISE, vs4daysVolumeDiff.abs().toPlainString()});
            } else if (vs4daysVolumeDiff.compareTo(BigDecimal.ZERO) < 0) {
                detail.getTags().setTradingVolumeVS4DaysAgo(new String[]{CommonTerm.FALL, vs4daysVolumeDiff.abs().toPlainString()});
            } else {
                detail.getTags().setTradingVolumeVS4DaysAgo(new String[]{CommonTerm.UNCHANGED, vs4daysVolumeDiff.abs().toPlainString()});
            }
            BigDecimal vs4daysAmountDiff = second.getTodayTradingVolumeMoney()
                    .subtract(v.get(5).getTodayTradingVolumeMoney())
                    .divide(v.get(5).getTodayTradingVolumeMoney(), 4, RoundingMode.FLOOR)
                    .multiply(BigDecimal.valueOf(100));
            if (vs4daysAmountDiff.compareTo(BigDecimal.ZERO) > 0) {
                detail.getTags().setTradingAmountVS4DaysAgo(new String[]{CommonTerm.RISE, vs4daysAmountDiff.abs().toPlainString()});
            } else if (vs4daysAmountDiff.compareTo(BigDecimal.ZERO) < 0) {
                detail.getTags().setTradingAmountVS4DaysAgo(new String[]{CommonTerm.FALL, vs4daysAmountDiff.abs().toPlainString()});
            } else {
                detail.getTags().setTradingAmountVS4DaysAgo(new String[]{CommonTerm.UNCHANGED, vs4daysAmountDiff.abs().toPlainString()});
            }

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
        DailyMetricsCalculator metricsCalculator = new DailyMetricsCalculator();

        stockIdToInfoDTOs.forEach((k, v) -> {
            v.sort(Comparator.comparingLong(StockInfo4InitMetricsDTO::getDate).reversed());
            StockInfo4InitMetricsDTO first = v.getFirst();//最新交易日info
            DailyStockMetricsDTO metrics = stockIdToMetricsDTO.get(k);
            DailyStockMetricsDTO result = new DailyStockMetricsDTO();
            result.setStockId(k);
            result.setStockName(first.getStockName());
            result.setDate(first.getDate());
            result.setTodayClosingPrice(first.getTodayClosingPrice());

            metricsCalculator.cal(
                    result,
                    v.getFirst(),
                    v.get(1),
                    v.get(2),
                    v.get(3),
                    v.get(4),
                    v.get(5),
                    v.get(6),
                    metrics
            );

            //填充本日到最新交易日之間非週六週日的資料
            LocalDate tempDate = LocalDate.parse(first.getDate().toString(), DatePattern.PURE_DATE_FORMATTER).minusDays(1);
            while (tempDate.isBefore(now)) {
                if (tempDate.getDayOfWeek() != DayOfWeek.SATURDAY && tempDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                    DailyStockMetricsDTO copied = BeanUtil.copyProperties(result, DailyStockMetricsDTO.class);
                    copied.setDate(Long.parseLong(tempDate.format(DatePattern.PURE_DATE_FORMATTER)));
                    copied.setTodayClosingPrice(null);
                    results.add(copied);
                }
                tempDate = tempDate.minusDays(1);
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

            detailCalculator.cal(
                    detail,
                    v.getFirst(),
                    v.get(1),
                    v.get(2),
                    v.get(3),
                    stockIdToMetricsDTOs.get(k).getFirst(),
                    stockIdToMetricsDTOs.get(k).getLast(),
                    stockIdToDetailDTO.get(k)
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
}
