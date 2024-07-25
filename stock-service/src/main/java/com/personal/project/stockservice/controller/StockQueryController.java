package com.personal.project.stockservice.controller;

import cn.hutool.core.date.DatePattern;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.PageInfo;
import com.personal.project.commoncore.constants.ResponseCode;
import com.personal.project.commoncore.response.CommonResponse;
import com.personal.project.stockservice.model.dto.request.QueryConditionDTO;
import com.personal.project.stockservice.model.dto.request.QueryConditionRealTimeDTO;
import com.personal.project.stockservice.model.dto.response.CompleteStockDTO;
import com.personal.project.stockservice.model.dto.response.DailyStockInfoDTO;
import com.personal.project.stockservice.model.dto.response.RealTimeStockDTO;
import com.personal.project.stockservice.service.DailyStockInfoService;
import jakarta.validation.constraints.Min;
import org.hibernate.validator.constraints.Range;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/v1/stock")
public class StockQueryController {

	private final DailyStockInfoService dailyStockInfoService;

	public StockQueryController(DailyStockInfoService dailyStockInfoService) {
		this.dailyStockInfoService = dailyStockInfoService;
	}

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
			return CommonResponse.error(ResponseCode.Not_Valid.getCode(), "Today is not trading day", null);
		}

		if (now.toLocalTime().isAfter(LocalTime.of(14, 0))) {
			return CommonResponse.error(ResponseCode.Not_Valid.getCode(), "Now is off trading, use other api to check today's data", null);
		}

		List<RealTimeStockDTO> results = dailyStockInfoService.conditionRealTimeQuery(Long.parseLong(nowStr), dto);

		return CommonResponse.ok(results);
	}
}
