package com.personal.project.stockservice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personal.project.stockservice.model.dto.ConditionQueryUnionInfoDTO;
import com.personal.project.stockservice.model.dto.QueryConditionDTO;

public interface StockQueryService {

    IPage<ConditionQueryUnionInfoDTO> conditionQuery(Page<ConditionQueryUnionInfoDTO> page, QueryConditionDTO dto);
}
