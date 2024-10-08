package com.personal.project.reportservice.job;


import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.personal.project.commoncore.constants.ResponseCode;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.reportservice.model.entity.ReportErrorMessageDO;
import com.personal.project.reportservice.service.GenerateReportService;
import com.personal.project.reportservice.service.ReportErrorMessageService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Component
@Slf4j
public class ReportJob {

	public static final String DAILY_STOCK_METRICS = "daily stock metrics";
	public static final String DAILY_STOCK_DETAIL = "daily stock detail";

	private final ReportErrorMessageService reportErrorMessageService;
	private final GenerateReportService generateReportService;

	public ReportJob(ReportErrorMessageService reportErrorMessageService, GenerateReportService generateReportService) {
		this.reportErrorMessageService = reportErrorMessageService;
		this.generateReportService = generateReportService;
	}

	/**
	 * 產出每支個股每日各項指標資訊
	 */
	@XxlJob("generateDailyMetrics")
	public void generateDailyMetrics() {
		LocalDate now = LocalDate.now();
		String nowStr = now.format(DatePattern.PURE_DATE_FORMATTER);

		//檢查當下日期, 週六日則停止
		if (now.getDayOfWeek() == DayOfWeek.SATURDAY || now.getDayOfWeek() == DayOfWeek.SUNDAY) {
			log.warn("generateDailyMetrics 停止, 今日非平日, date: {}", now);
			generateAndSaveReportErrorMessage(DAILY_STOCK_METRICS, "Today is not trading day " + now, null, null, null);
			return;
		}

		InnerResponse<ObjectUtils.Null> saved = generateReportService.generateRoutineMetrics(Long.parseLong(nowStr));

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
		LocalDate now = LocalDate.now();
		String nowStr = now.format(DatePattern.PURE_DATE_FORMATTER);

		if (now.getDayOfWeek() == DayOfWeek.SATURDAY || now.getDayOfWeek() == DayOfWeek.SUNDAY) {
			log.warn("generateDailyDetail 停止, 今日非交易日 date: {}", now);
			generateAndSaveReportErrorMessage(DAILY_STOCK_DETAIL, "Today is not trading day " + now, null, null, null);
			return;
		}

		InnerResponse<ObjectUtils.Null> saved = generateReportService.generateRoutineDetail(Long.parseLong(nowStr));

		if (!ResponseCode.Success.getCode().equals(saved.getCode())) {
			log.error("Calculate detail save detail failed");
			generateAndSaveReportErrorMessage(DAILY_STOCK_DETAIL, saved.getMsg(), null, null, JSONUtil.toJsonStr(saved));
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
}
