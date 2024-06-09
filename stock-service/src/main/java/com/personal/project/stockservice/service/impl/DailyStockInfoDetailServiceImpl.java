package com.personal.project.stockservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.personal.project.stockservice.mapper.DailyStockInfoDetailMapper;
import com.personal.project.stockservice.model.dto.*;
import com.personal.project.stockservice.model.entity.DailyStockInfoDetailDO;
import com.personal.project.stockservice.service.DailyStockInfoDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DailyStockInfoDetailServiceImpl extends ServiceImpl<DailyStockInfoDetailMapper, DailyStockInfoDetailDO> implements DailyStockInfoDetailService {

    @Override
    public List<DailyStockInfoDetailDTO> query4CalDetail(Query4CalDTO query4CalDTO) {

        return baseMapper.query4CalDetail(query4CalDTO);
    }

    @Override
    public List<DailyStockInfoDetailDTO> query4CalDetail(ManualCalDTO manualCalDTO) {
        return baseMapper.query4CalDetailManual(manualCalDTO);
    }

    @Override
    public List<SimpleDetailDTO> queryDetail(Long date) {
        QueryWrapper<DailyStockInfoDetailDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "stock_id")
                .eq("date", date);

        List<Map<String, Object>> maps = baseMapper.selectMaps(queryWrapper);

        return maps.stream()
                .map(o -> new SimpleDetailDTO(o.get("stock_id").toString(), Long.valueOf(o.get("id").toString())))
                .toList();
    }

    @Override
    public IPage<ConditionQueryUnionInfoDTO> queryCondition(Page<ConditionQueryUnionInfoDTO> page, QueryConditionDTO dto, Long date) {

        return baseMapper.queryCondition(page, dto, date);
    }
}
