package com.personal.project.reportservice.service;

import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.reportservice.model.dto.ManualCalReportDTO;
import org.apache.commons.lang3.ObjectUtils;

public interface GenerateReportService {

    InnerResponse<ObjectUtils.Null> generateInitYesterdayMetricReport();

    InnerResponse<ObjectUtils.Null> generateInitYesterdayDetailReport();

    InnerResponse<ObjectUtils.Null> generateInitTodayMetricReport();

    InnerResponse<ObjectUtils.Null> generateInitTodayDetailReport();
}
