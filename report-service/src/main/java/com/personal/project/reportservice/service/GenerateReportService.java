package com.personal.project.reportservice.service;

import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.reportservice.model.dto.DailyStockMetricsDTO;
import org.apache.commons.lang3.ObjectUtils;

public interface GenerateReportService {

    InnerResponse<ObjectUtils.Null> generateInitYesterdayMetricReport();

    InnerResponse<ObjectUtils.Null> generateInitYesterdayDetailReport();

    InnerResponse<ObjectUtils.Null> generateInitTodayMetricReport();

    InnerResponse<ObjectUtils.Null> generateInitTodayDetailReport();

    InnerResponse<ObjectUtils.Null> generateRoutineMetrics(Long date);

    InnerResponse<ObjectUtils.Null> generateRealTimeMetrics(Long date);

    InnerResponse<ObjectUtils.Null> generateRoutineDetail(Long date);

    InnerResponse<ObjectUtils.Null> generateRealTimeDetail(Long date);

    DailyStockMetricsDTO generateRoutineMetricsOldSchool(String stockId, Long date);
}
