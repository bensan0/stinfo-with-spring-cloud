package com.personal.project.reportservice.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.reportservice.model.dto.*;
import com.personal.project.reportservice.remote.RemoteStockService;
import com.personal.project.reportservice.service.GenerateReportService;
import com.personal.project.reportservice.util.ReportHelper;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
public class GenerateReportServiceImpl implements GenerateReportService {

    private static final Logger log = LoggerFactory.getLogger(GenerateReportServiceImpl.class);
    private final ReportHelper reportHelper;

    private final RemoteStockService remoteStockService;

    public GenerateReportServiceImpl(ReportHelper reportHelper, RemoteStockService remoteStockService) {
        this.reportHelper = reportHelper;
        this.remoteStockService = remoteStockService;
    }

    /**
     * 手動觸發重新計算某日指標
     *
     * @param dto
     * @return
     */
    @Override
    public InnerResponse<ObjectUtils.Null> generateMetricReport(ManualCalReportDTO dto) {
        LocalDate localDate = LocalDateTimeUtil.parseDate(dto.getDate(), DatePattern.PURE_DATE_FORMATTER);

        Query4ManualCalDTO req = new Query4ManualCalDTO(
                Long.valueOf(localDate.format(DatePattern.PURE_DATE_FORMATTER)),
                localDate.getDayOfWeek() == DayOfWeek.MONDAY ? Long.valueOf(localDate.minusDays(3).format(DatePattern.PURE_DATE_FORMATTER)) : Long.valueOf(localDate.minusDays(1).format(DatePattern.PURE_DATE_FORMATTER))
        );

        InnerResponse<CalMetricsUnionDTO> response = remoteStockService.getCalMetricsInfo(req, null);

        List<DailyStockMetricsDTO> dailyStockMetricsDTOs = reportHelper.calDailyMetrics(response.getData());

        return remoteStockService.saveMetrics(dailyStockMetricsDTOs, null);
    }


    @Override
    public InnerResponse<ObjectUtils.Null> generateDetailReport(ManualCalReportDTO dto) {
        LocalDate localDate = LocalDateTimeUtil.parseDate(dto.getDate(), DatePattern.PURE_DATE_FORMATTER);

        Query4ManualCalDTO req = new Query4ManualCalDTO(
                Long.valueOf(localDate.format(DatePattern.PURE_DATE_FORMATTER)),
                localDate.getDayOfWeek() == DayOfWeek.MONDAY ? Long.valueOf(localDate.minusDays(3).format(DatePattern.PURE_DATE_FORMATTER)) : Long.valueOf(localDate.minusDays(1).format(DatePattern.PURE_DATE_FORMATTER))
        );

        InnerResponse<CalDetailUnionDTO> response = remoteStockService.getCalDetailInfo(req, null);

        List<DailyStockInfoDetailDTO> dailyStockInfoDetailDTOs = reportHelper.calDailyDetail(response.getData());

        return null;
    }

}
