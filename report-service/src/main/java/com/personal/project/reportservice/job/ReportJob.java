package com.personal.project.reportservice.job;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.personal.project.commoncore.constants.ResponseCode;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.reportservice.model.dto.*;
import com.personal.project.reportservice.model.entity.ReportErrorMessageDO;
import com.personal.project.reportservice.remote.RemoteStockService;
import com.personal.project.reportservice.service.ReportErrorMessageService;
import com.personal.project.reportservice.util.DailyDetailCalculator;
import com.personal.project.reportservice.util.DailyMetricsCalculator;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ReportJob {

    public static final String DAILY_STOCK_METRICS = "daily stock metrics";
    public static final String DAILY_STOCK_DETAIL = "daily stock detail";

    private final RemoteStockService remoteStockService;
    private final ReportErrorMessageService reportErrorMessageService;

    public ReportJob(RemoteStockService remoteStockService, ReportErrorMessageService reportErrorMessageService) {
        this.remoteStockService = remoteStockService;
        this.reportErrorMessageService = reportErrorMessageService;
    }

    /**
     * 產出每支個股每日各項指標資訊
     */
    @XxlJob("generateDailyMetrics")
    public void generateDailyMetrics() {
        LocalDate now = LocalDate.now();
        String nowStr = now.format(DatePattern.PURE_DATE_FORMATTER);
        //todo 先檢查redis flag

        //檢查當下日期, 週六日則停止
        if (now.getDayOfWeek() == DayOfWeek.SATURDAY || now.getDayOfWeek() == DayOfWeek.SUNDAY) {
            log.warn("generateDailyMetrics 停止, 今日非平日, date: {}", now);
            generateAndSaveReportErrorMessage(DAILY_STOCK_METRICS, "Today is not trading day " + now, null, null, null);
            return;
        }

        InnerResponse<CalMetricsUnionDTO> response = remoteStockService.getCalMetricsInfo(new Query4CalMetricsDTO(Long.parseLong(now.format(DatePattern.PURE_DATE_FORMATTER))), null);
        Map<String, List<DailyStockMetricsDTO>> stockIdToMetrics = response.getData().getStockIdToMetrics();
        Map<String, List<DailyStockInfoDTO>> stockIdToInfos = response.getData().getStockIdToInfos();
        List<DailyStockMetricsDTO> results = new ArrayList<>();
        DailyMetricsCalculator metricsCalculator = new DailyMetricsCalculator();

        stockIdToInfos.forEach((k, v) -> {
            v.sort(Comparator.comparingLong(DailyStockInfoDTO::getDate).reversed());
            DailyStockMetricsDTO todayMetrics;
            DailyStockMetricsDTO yesterdayMetrics;
            List<DailyStockMetricsDTO> metrics = stockIdToMetrics.get(k);
            metrics.sort(Comparator.comparingLong(DailyStockMetricsDTO::getDate).reversed());

            //metrics任務沒被手動提前按
            if (metrics.size() == 1) {
                todayMetrics = new DailyStockMetricsDTO();
                todayMetrics.setStockId(k);
                todayMetrics.setStockName(v.getFirst().getStockName());
                todayMetrics.setDate(Long.parseLong(nowStr));
                yesterdayMetrics = metrics.getFirst();
            } else {
                todayMetrics = metrics.getFirst();
                yesterdayMetrics = metrics.getLast();
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

            metricsCalculator.cal(todayMetrics,
                    BeanUtil.copyProperties(v.getFirst(), StockInfo4InitMetricsDTO.class),
                    BeanUtil.copyProperties(v.get(1), StockInfo4InitMetricsDTO.class),
                    BeanUtil.copyProperties(v.get(2), StockInfo4InitMetricsDTO.class),
                    BeanUtil.copyProperties(v.get(3), StockInfo4InitMetricsDTO.class),
                    BeanUtil.copyProperties(v.get(4), StockInfo4InitMetricsDTO.class),
                    BeanUtil.copyProperties(v.get(5), StockInfo4InitMetricsDTO.class),
                    BeanUtil.copyProperties(v.getLast(), StockInfo4InitMetricsDTO.class),
                    yesterdayMetrics);

            results.add(todayMetrics);
        });

        //call feign save
        InnerResponse<ObjectUtils.Null> saved = remoteStockService.saveMetrics(results, null);

        if (!ResponseCode.Success.getCode().equals(saved.getCode())) {
            log.error("Calculate metrics save metrics failed");
            generateAndSaveReportErrorMessage(DAILY_STOCK_METRICS, saved.getMsg(), null, null, JSONUtil.toJsonStr(saved));
        }
    }

    /**
     * 產出每日個股Ｋ線標籤報告
     */
    @XxlJob("generateDailyDetail")
    public void generateDailyDetail() {
        //todo 先檢查redis flag
        LocalDate now = LocalDate.now();
        String nowStr = now.format(DatePattern.PURE_DATE_FORMATTER);

        if (now.getDayOfWeek() == DayOfWeek.SATURDAY || now.getDayOfWeek() == DayOfWeek.SUNDAY) {
            log.warn("generateDailyDetail 停止, 今日非交易日 date: {}", now);
            generateAndSaveReportErrorMessage(DAILY_STOCK_DETAIL, "Today is not trading day " + now, null, null, null);
            return;
        }

        Query4CalMetricsDTO dto = new Query4CalMetricsDTO(
                Long.valueOf(nowStr)
        );
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
            List<DailyStockInfoDetailDTO> details = stockIdToDetails.get(k);
            DailyStockInfoDetailDTO todayDetail;
            DailyStockInfoDetailDTO yesterdayDetail;

            //本日有沒有提前按過
            if (details.size() == 1) {
                todayDetail = new DailyStockInfoDetailDTO();
                todayDetail.setStockId(k);
                todayDetail.setDate(Long.parseLong(nowStr));
                yesterdayDetail = details.getFirst();
            } else {
                details.sort(Comparator.comparingLong(DailyStockInfoDetailDTO::getDate).reversed());
                todayDetail = details.getFirst();
                yesterdayDetail = details.getLast();
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
                todayDetail.setTodayClosingPrice(v.removeFirst().getTodayClosingPrice());
            }

            calculator.cal(
                    todayDetail,
                    v.getFirst(),
                    v.get(1),
                    v.get(2),
                    v.get(3),
                    metrics.getFirst(),
                    metrics.getLast(),
                    yesterdayDetail
            );

            results.add(todayDetail);
        });

        //call feign save
        InnerResponse<ObjectUtils.Null> response = remoteStockService.saveDetail(results, null);
    }

    private void generateAndSaveReportErrorMessage(String reportName,
                                                   String errorMsg,
                                                   String exceptionName,
                                                   String exceptionMsg,
                                                   String extra) {
        ReportErrorMessageDO errorMessage = new ReportErrorMessageDO();
        errorMessage.setReportName(reportName);
        errorMessage.setDate(DateUtil.now());
        errorMessage.setErrorMessage(errorMsg);
        errorMessage.setException(exceptionName);
        errorMessage.setExceptionMessage(exceptionMsg);
        errorMessage.setExtra(extra);
        reportErrorMessageService.save(errorMessage);
    }
}
