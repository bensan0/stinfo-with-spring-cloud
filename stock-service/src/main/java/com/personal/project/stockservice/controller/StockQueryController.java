package com.personal.project.stockservice.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personal.project.commoncore.response.CommonResponse;
import com.personal.project.commoncore.response.PageResponse;
import com.personal.project.stockservice.model.dto.ConditionQueryUnionInfoDTO;
import com.personal.project.stockservice.model.dto.QueryConditionDTO;
import com.personal.project.stockservice.service.StockQueryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/stock")
public class StockQueryController {

    private final StockQueryService stockQueryService;

    public StockQueryController(StockQueryService stockQueryService) {
        this.stockQueryService = stockQueryService;
    }

    //todo 當日股票概要列表
    @GetMapping("/summary/list")
    public CommonResponse summary() {
        return null;
    }

    //todo 目前條件之間一律用and, 之後優化成條件之間的and or 可以從前台選擇
    //todo 暫定僅搜尋今日, 之後優化成可搜尋過去特定日期
    @PostMapping("/condition/list")
    public PageResponse<Object> conditionQuery(
            @RequestParam Integer pageSize,
            @RequestParam Integer currentPage,
            @RequestBody QueryConditionDTO dto,
            @RequestHeader("token") String token
    ) {
        Page<ConditionQueryUnionInfoDTO> page = new Page<>(currentPage, pageSize);

        IPage<ConditionQueryUnionInfoDTO> result = stockQueryService.conditionQuery(page, dto);

        return PageResponse.ok(page.getCurrent(), page.getTotal(), result);
    }

}
