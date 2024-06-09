package com.personal.project.reportservice.job;


import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONUtil;
import com.personal.project.commoncore.constants.ResponseCode;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.reportservice.model.dto.*;
import com.personal.project.reportservice.model.entity.ReportErrorMessageDO;
import com.personal.project.reportservice.remote.RemoteStockService;
import com.personal.project.reportservice.service.ReportErrorMessageService;
import com.personal.project.reportservice.util.ReportHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class ReportJob {

    public static final String DAILY_STOCK_METRICS = "daily stock metrics";
    public static final String DAILY_STOCK_DETAIL = "daily stock detail";
    private final ReportHelper reportHelper;

    private final RemoteStockService remoteStockService;
    private final ReportErrorMessageService reportErrorMessageService;

    public ReportJob(ReportHelper reportHelper, RemoteStockService remoteStockService, ReportErrorMessageService reportErrorMessageService) {
        this.reportHelper = reportHelper;
        this.remoteStockService = remoteStockService;
        this.reportErrorMessageService = reportErrorMessageService;
    }

    /**
     * 產出每支個股每日各項指標資訊
     * 方式：
     * 獲取昨日指標報告,
     * 獲取包含今日5交易日前, 10交易日前, 20交易日前...收盤價資料
     * 計算新5日, 10日, 20日...平均價
     * 儲存今日指標報告
     */
    @XxlJob("generateDailyMetrics")
    public void generateDailyMetrics() {
        //todo 先檢查redis flag
        LocalDateTime now = LocalDateTimeUtil.now();
        if (isNotTradingDay(now)) {
            generateAndSaveReportErrorMessage(DAILY_STOCK_METRICS, "Today is not trading day " + now, null, null, null);
            return;
        }

        //上個交易日直接用昨日取代之原因為：若只取前一個交易日, 則因非交易日之平日時, 指標會自動複製前一日的資料, 因此昨日一定為上一個交易日的資料
        Query4CalMetricsDTO dto = new Query4CalMetricsDTO(
                Long.valueOf(now.format(DatePattern.PURE_DATE_FORMATTER)),
                now.getDayOfWeek() == DayOfWeek.MONDAY ? Long.valueOf(now.minusDays(3).format(DatePattern.PURE_DATE_FORMATTER)) : Long.valueOf(now.minusDays(1).format(DatePattern.PURE_DATE_FORMATTER))
        );
        InnerResponse<CalMetricsUnionDTO> unionDTO = remoteStockService.getCalMetricsInfo(dto, null);

        //check
        if(unionDTO.getData().getCalMetricsInfo().isEmpty()){
            log.warn("Calculate metrics failed, CalMetricsInfo is empty");
            generateAndSaveReportErrorMessage(DAILY_STOCK_METRICS, "Calculate metrics failed, CalMetricsInfo is empty", null, null, null);
        }

        //cal
        List<DailyStockMetricsDTO> dailyStockMetricsDTOs = reportHelper.calDailyMetrics(unionDTO.getData());

        //call feign save
        InnerResponse<ObjectUtils.Null> response = remoteStockService.saveMetrics(dailyStockMetricsDTOs, null);

        if (!ResponseCode.Success.getCode().equals(response.getCode())) {
            log.error("Calculate metrics save metrics failed");
            generateAndSaveReportErrorMessage(DAILY_STOCK_METRICS, response.getMsg(), null, null, JSONUtil.toJsonStr(response));
        }
    }

    /**
     * 產出每日個股Ｋ線標籤報告
     */
    @XxlJob("generateDailyDetail")
    public void generateDailyDetail() {
        //todo 先檢查redis flag
        LocalDateTime now = LocalDateTimeUtil.now();
        if (isNotTradingDay(now)) {
            generateAndSaveReportErrorMessage(DAILY_STOCK_DETAIL, "Today is not trading day " + now, null, null, null);
            return;
        }

        //todo 優化查詢
        //取得今日,3個交易日前(含今日), 5個交易日前(含今日)個股資料 ＆ 獲取本日已存在個股標籤數據 & 獲取前一個交易日個股標籤數據 & 取得今日個股指標 & 上一個交易日個股指標
        Query4CalMetricsDTO dto = new Query4CalMetricsDTO(
                Long.valueOf(now.format(DatePattern.PURE_DATE_FORMATTER)),
                Long.valueOf(now.minusDays(1).format(DatePattern.PURE_DATE_FORMATTER))
        );
        InnerResponse<CalDetailUnionDTO> calDetailInfo = remoteStockService.getCalDetailInfo(dto, null);

        //cal
        List<DailyStockInfoDetailDTO> dailyStockInfoDetailDTOS = reportHelper.calDailyDetail(calDetailInfo.getData());

        //call feign save
        InnerResponse<ObjectUtils.Null> response = remoteStockService.saveDetail(dailyStockInfoDetailDTOS, null);

        if (!ResponseCode.Success.getCode().equals(response.getCode())) {
            log.error("Calculate detail save detail failed");
            generateAndSaveReportErrorMessage(DAILY_STOCK_DETAIL, response.getMsg(), null, null, JSONUtil.toJsonStr(response));
        }

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

    private boolean isNotTradingDay(LocalDateTime localDateTime) {
        DayOfWeek dayOfWeek = localDateTime.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
}
