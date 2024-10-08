package com.personal.project.stockservice.controller;

import cn.hutool.core.date.DatePattern;
import com.github.pagehelper.PageInfo;
import com.personal.project.commoncore.constants.ResponseCode;
import com.personal.project.commoncore.response.CommonResponse;
import com.personal.project.stockservice.model.dto.request.DailyIndexInfoDTO;
import com.personal.project.stockservice.model.dto.request.QueryConditionDTO;
import com.personal.project.stockservice.model.dto.request.QueryConditionRealTimeDTO;
import com.personal.project.stockservice.model.dto.response.CompleteStockDTO;
import com.personal.project.stockservice.model.dto.response.DailyStockInfoDTO;
import com.personal.project.stockservice.model.dto.response.RealTimeStockDTO;
import com.personal.project.stockservice.service.DailyIndexInfoService;
import com.personal.project.stockservice.service.DailyStockInfoService;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Range;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/v1/stock")
@Slf4j
@AllArgsConstructor
public class StockQueryController {

	private final DailyStockInfoService dailyStockInfoService;

	private final DailyIndexInfoService dailyIndexInfoService;

	@GetMapping("/{stockId}/list")
	public CommonResponse<PageInfo<DailyStockInfoDTO>> getStockList(
			@PathVariable("stockId") String stockId,
			@Min(1) @RequestParam(defaultValue = "1") Integer current,
			@Range(min = 10, max = 100) @RequestParam(defaultValue = "10") Integer size
	) {
		PageInfo<DailyStockInfoDTO> page = dailyStockInfoService.queryByStockId(stockId, current, size);

		return CommonResponse.ok(page);
	}

	@PostMapping("/condition/list")
	public CommonResponse<PageInfo<CompleteStockDTO>> conditionQuery(
			@Min(1) @RequestParam(defaultValue = "1") Integer current,
			@Range(min = 10, max = 100) @RequestParam(defaultValue = "10") Integer size,
			@RequestBody QueryConditionDTO dto
	) {
		PageInfo<CompleteStockDTO> page = dailyStockInfoService.conditionQuery(current, size, dto);

		return CommonResponse.ok(page);
	}

	@PostMapping("/condition/real-time/list")
	public CommonResponse<List<RealTimeStockDTO>> conditionRealTimeQuery(
			@RequestBody QueryConditionRealTimeDTO dto
	) {
		LocalDateTime now = LocalDateTime.now();
		String nowStr = now.format(DatePattern.PURE_DATE_FORMATTER);
		if (now.getDayOfWeek() == DayOfWeek.SATURDAY || now.getDayOfWeek() == DayOfWeek.SUNDAY) {
			return CommonResponse.error(ResponseCode.Not_Valid.getCode(), "本日非交易日", null);
		}

		if (now.toLocalTime().isAfter(LocalTime.of(14, 30))) {
			return CommonResponse.error(ResponseCode.Not_Valid.getCode(), "本日已收盤", null);
		}

		List<RealTimeStockDTO> results = dailyStockInfoService.conditionRealTimeQuery(Long.parseLong(nowStr), dto);

		return CommonResponse.ok(results);
	}

	@GetMapping("/index/latest")
	public CommonResponse<List<DailyIndexInfoDTO>> indexQuery() {
		List<DailyIndexInfoDTO> data = dailyIndexInfoService.queryLatest();

		return CommonResponse.ok(data);
	}
}
