package com.personal.project.stockservice.controller;

import com.github.pagehelper.PageInfo;
import com.personal.project.commoncore.response.CommonResponse;
import com.personal.project.stockservice.model.dto.CompleteStockDTO;
import com.personal.project.stockservice.model.dto.ConditionQueryUnionInfoDTO;
import com.personal.project.stockservice.model.dto.DailyStockInfoDTO;
import com.personal.project.stockservice.model.dto.QueryConditionDTO;
import com.personal.project.stockservice.service.DailyStockInfoService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/stock")
public class StockQueryController {

	private final DailyStockInfoService dailyStockInfoService;

	public StockQueryController(DailyStockInfoService dailyStockInfoService) {
		this.dailyStockInfoService = dailyStockInfoService;
	}

	//todo 當日股票概要列表
//	@GetMapping("/today-list")
//	public CommonResponse<PageDTO<DailyStockInfoDTO>> getTodayInfos(
//			@NotNull @Min(1) @RequestParam Integer current,
//			@RequestParam @NotNull @Range(min = 10, max = 100) Integer size
//	) {
//		PageDTO<DailyStockInfoDTO> dailyStockInfoDTOPageDTO = dailyStockInfoService.queryTodayInfos(current, size);
//		return null;
//	}

	@GetMapping("/{stockId}/list")
	public CommonResponse<PageInfo<DailyStockInfoDTO>> getStockList(
			@PathVariable("stockId") String stockId,
			@Min(1) @RequestParam(defaultValue = "1") Integer current,
			@Range(min = 10, max = 100) @RequestParam(defaultValue = "10") Integer size
	) {
		PageInfo<DailyStockInfoDTO> page = dailyStockInfoService.queryByStockId(stockId, current, size);

		return CommonResponse.ok(page);
	}


    //todo 目前條件之間一律用and, 之後優化成條件之間的and or 可以從前台選擇
    //todo 暫定僅搜尋今日, 之後優化成可搜尋過去特定日期
    @PostMapping("/condition/list")
    public CommonResponse<PageInfo<CompleteStockDTO>> conditionQuery(
            @Min(1) @RequestParam(defaultValue = "1") Integer current,
            @Range(min = 10, max = 100) @RequestParam(defaultValue = "10") Integer size,
            @RequestBody QueryConditionDTO dto
    ) {
		PageInfo<CompleteStockDTO> page = dailyStockInfoService.conditionQuery(current, size, dto);

		return CommonResponse.ok(page);
    }

}
