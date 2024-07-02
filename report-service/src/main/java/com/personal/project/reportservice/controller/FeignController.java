package com.personal.project.reportservice.controller;

import com.personal.project.commoncore.constants.ResponseCode;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.reportservice.service.GenerateReportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feign/report")
@Slf4j
public class FeignController {

    private final GenerateReportService generateReportService;

    public FeignController(GenerateReportService generateReportService) {
        this.generateReportService = generateReportService;
    }


    @PostMapping("/init-yesterday-report")
    public InnerResponse<ObjectUtils.Null> calInitYesterdayReport() {

        InnerResponse<ObjectUtils.Null> response = generateReportService.generateInitYesterdayMetricReport();
        if (!ResponseCode.Success.getCode().equals(response.getCode())) {
            return response;
        }

        return generateReportService.generateInitYesterdayDetailReport();
    }

    @PostMapping("/init-today-report")
    public InnerResponse<ObjectUtils.Null> calInitTodayReport() {
        InnerResponse<ObjectUtils.Null> response = generateReportService.generateInitTodayMetricReport();
        if(!ResponseCode.Success.getCode().equals(response.getCode())){
            return response;
        }

        return generateReportService.generateInitTodayDetailReport();
    }
}
