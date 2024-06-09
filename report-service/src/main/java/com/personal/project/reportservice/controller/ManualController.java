package com.personal.project.reportservice.controller;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.personal.project.commoncore.constants.ResponseCode;
import com.personal.project.commoncore.response.CommonResponse;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.reportservice.model.dto.ManualCalReportDTO;
import com.personal.project.reportservice.service.GenerateReportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.time.DateTimeException;
import java.time.DayOfWeek;

@RestController
@RequestMapping("/report")
@Slf4j
public class ManualController {

    private final GenerateReportService generateReportService;

    public ManualController(GenerateReportService generateReportService) {
        this.generateReportService = generateReportService;
    }

    //todo 現階段僅能輸入一日, 並重新計算該日所有指標, 未來優化為更彈性的作法(日期區間, 特定(組)個股..等)
    //todo 若是重算發現的確有誤, 則此日往後之所有指標與標籤都需要重新計算, 是否獨立設置ＶＴ方法背景執行
    @PostMapping("/date/manual-metric-cal")
    public CommonResponse<ObjectUtils.Null> manualMetricByDate(
            @RequestBody ManualCalReportDTO dto,
            @RequestHeader String token
    ) {
        CommonResponse<ObjectUtils.Null> checkResult = dateCheck(dto.getDate());
        if(checkResult != null){
            return checkResult;
        }

        InnerResponse<ObjectUtils.Null> result = generateReportService.generateMetricReport(dto);

        return ResponseCode.Success.getCode().equals(result.getCode()) ? CommonResponse.ok(null) : CommonResponse.error(result.getCode(), result.getMsg(), null);
    }

    //todo 同上
    @PostMapping("/date/manual-detail-cal")
    public CommonResponse<ObjectUtils.Null> manualDetailByDate(
            @RequestBody ManualCalReportDTO dto,
            @RequestHeader String token
    ) {
        CommonResponse<ObjectUtils.Null> checkResult = dateCheck(dto.getDate());
        if(checkResult != null){
            return checkResult;
        }

        InnerResponse<ObjectUtils.Null> result = generateReportService.generateDetailReport(dto);

        return ResponseCode.Success.getCode().equals(result.getCode()) ? CommonResponse.ok(null) : CommonResponse.error(result.getCode(), result.getMsg(), null);
    }

    private CommonResponse<ObjectUtils.Null> dateCheck(String date) {
        try {
            if (date.isEmpty() || !StrUtil.isNumeric(date)) {

                return CommonResponse.error(ResponseCode.Invalid_Args, "Date should be numeric", null);
            } else if (LocalDateTimeUtil.parseDate(date, DatePattern.PURE_DATE_FORMATTER).getDayOfWeek() == DayOfWeek.SATURDAY ||
                    LocalDateTimeUtil.parseDate(date, DatePattern.PURE_DATE_FORMATTER).getDayOfWeek() == DayOfWeek.SUNDAY) {

                return CommonResponse.error(ResponseCode.Invalid_Args, "Date can not be Saturday or Sunday", null);
            }
        } catch (DateTimeException e) {

            return CommonResponse.error(ResponseCode.Invalid_Args, "Date format go wrong", null);
        }

        return null;
    }

}
