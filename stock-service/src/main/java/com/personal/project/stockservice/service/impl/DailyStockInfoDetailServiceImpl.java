package com.personal.project.stockservice.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.personal.project.stockservice.mapper.DailyStockInfoDetailMapper;
import com.personal.project.stockservice.model.dto.*;
import com.personal.project.stockservice.model.entity.DailyStockInfoDetailDO;
import com.personal.project.stockservice.service.DailyStockInfoDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DailyStockInfoDetailServiceImpl extends ServiceImpl<DailyStockInfoDetailMapper, DailyStockInfoDetailDO> implements DailyStockInfoDetailService {

    @Override
    public Map<String, List<DailyStockInfoDetailDTO>> query4CalDetail(Long date) {
        List<DailyStockInfoDetailDTO> dtos = baseMapper.query4CalDetail(date);
        Map<String, List<DailyStockInfoDetailDTO>> results = new HashMap<>();
        dtos.forEach(dto -> {
            results.computeIfAbsent(dto.getStockId(), k -> new ArrayList<>()).add(dto);
        });

        return results;
    }

    @Override
    public List<DailyStockInfoDetailDTO> query4CalDetail(ManualCalDTO manualCalDTO) {
        return baseMapper.query4CalDetailManual(manualCalDTO);
    }

    @Override
    public IPage<ConditionQueryUnionInfoDTO> queryCondition(Page<ConditionQueryUnionInfoDTO> page, QueryConditionDTO dto, Long date) {

        return baseMapper.queryCondition(page, dto, date);
    }

    @Override
    public Map<String, DailyStockInfoDetailDTO> query4InitTodayDetail() {

        List<DailyStockInfoDetailDTO> dtos = baseMapper.query4InitTodayDetail();

        return dtos.stream().collect(Collectors.toMap(DailyStockInfoDetailDTO::getStockId, Function.identity()));
    }
}
