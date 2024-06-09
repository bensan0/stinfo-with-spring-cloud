package com.personal.project.stockservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.personal.project.stockservice.mapper.DailyStockMetricsMapper;
import com.personal.project.stockservice.model.dto.*;
import com.personal.project.stockservice.model.entity.DailyStockMetricsDO;
import com.personal.project.stockservice.service.DailyStockMetricsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DailyStockMetricsServiceImpl extends ServiceImpl<DailyStockMetricsMapper, DailyStockMetricsDO>
        implements DailyStockMetricsService {

    @Override
    public List<SimpleMetricsDTO> queryMetrics(Long date) {
        QueryWrapper<DailyStockMetricsDO> wrapper = new QueryWrapper<>();
        wrapper
                .select("id", "stock_id")
                .eq("date", date);

        List<Map<String, Object>> maps = baseMapper.selectMaps(wrapper);

        return maps.stream().map(o -> new SimpleMetricsDTO(Long.valueOf(o.get("id").toString()), o.get("stock_id").toString()))
                .toList();
    }

    @Override
    public List<CalMetricsDTO> query4CalMetrics(Query4CalDTO query4CalDto) {

        return baseMapper.query4CalMetrics(query4CalDto);
    }

    @Override
    public List<CalMetricsDTO> query4CalMetrics(ManualCalDTO manualCalDTO) {

        return baseMapper.query4CalMetricsManual(manualCalDTO);
    }

    @Override
    public List<DailyStockMetricsDTO> query4CalDetail(Query4CalDTO query4CalDto) {

        return baseMapper.query4CalDetail(query4CalDto);
    }

    @Override
    public List<DailyStockMetricsDTO> query4CalDetail(ManualCalDTO manualCalDTO) {
        return baseMapper.query4ManualCalDetail(manualCalDTO);
    }


}
