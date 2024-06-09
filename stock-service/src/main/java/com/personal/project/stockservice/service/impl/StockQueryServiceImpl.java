package com.personal.project.stockservice.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personal.project.stockservice.model.dto.ConditionQueryUnionInfoDTO;
import com.personal.project.stockservice.model.dto.QueryConditionDTO;
import com.personal.project.stockservice.service.DailyStockInfoDetailService;
import com.personal.project.stockservice.service.StockQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StockQueryServiceImpl implements StockQueryService {

    private final DailyStockInfoDetailService dailyStockInfoDetailService;

    public StockQueryServiceImpl(DailyStockInfoDetailService dailyStockInfoDetailService) {
        this.dailyStockInfoDetailService = dailyStockInfoDetailService;
    }

    @Override
    public IPage<ConditionQueryUnionInfoDTO> conditionQuery(Page<ConditionQueryUnionInfoDTO> page, QueryConditionDTO dto) {
        Long today = Long.valueOf(LocalDateTimeUtil.now().format(DatePattern.PURE_DATE_FORMATTER));

        return dailyStockInfoDetailService.queryCondition(page, dto, today);
    }
}
