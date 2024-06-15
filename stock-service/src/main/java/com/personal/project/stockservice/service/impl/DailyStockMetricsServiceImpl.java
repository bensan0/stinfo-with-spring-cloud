package com.personal.project.stockservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.personal.project.stockservice.mapper.DailyStockMetricsMapper;
import com.personal.project.stockservice.model.dto.DailyStockMetricsDTO;
import com.personal.project.stockservice.model.dto.ManualCalDTO;
import com.personal.project.stockservice.model.dto.Query4CalDTO;
import com.personal.project.stockservice.model.entity.DailyStockMetricsDO;
import com.personal.project.stockservice.service.DailyStockMetricsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DailyStockMetricsServiceImpl extends ServiceImpl<DailyStockMetricsMapper, DailyStockMetricsDO>
        implements DailyStockMetricsService {

    @Override
    public Map<String, List<DailyStockMetricsDTO>> query4CalMetrics(Query4CalDTO query4CalDto) {
        List<DailyStockMetricsDTO> metrics = baseMapper.query4CalMetrics(query4CalDto);
        Map<String, List<DailyStockMetricsDTO>> stockIdToMetrics = new HashMap<>();
        metrics.forEach(dto -> stockIdToMetrics.computeIfAbsent(dto.getStockId(), k -> new ArrayList<>()).add(dto));

        return stockIdToMetrics;
    }

    @Override
    public Map<String, List<DailyStockMetricsDTO>> query4CalDetail(Query4CalDTO query4CalDto) {
        List<DailyStockMetricsDTO> dtos = baseMapper.query4CalDetail(query4CalDto);
        Map<String, List<DailyStockMetricsDTO>> results = new HashMap<>();
        dtos.forEach(dto -> {
            results.computeIfAbsent(dto.getStockId(), k -> new ArrayList<>()).add(dto);
        });

        return results;
    }

    @Override
    public List<DailyStockMetricsDTO> query4CalDetail(ManualCalDTO manualCalDTO) {

        return baseMapper.query4ManualCalDetail(manualCalDTO);
    }

    @Override
    public Map<String, DailyStockMetricsDTO> query4InitTodayMetrics() {
        List<DailyStockMetricsDTO> dtos = baseMapper.query4CalInitTodayMetrics();

        return dtos.stream().collect(Collectors.toMap(DailyStockMetricsDTO::getStockId, Function.identity()));
    }

    @Override
    public Map<String, List<DailyStockMetricsDTO>> query4InitTodayDetail() {
        List<DailyStockMetricsDTO> dtos = baseMapper.query4CalInitTodayDetail();
        Map<String, List<DailyStockMetricsDTO>> stockIdToMetricsDTOs = new HashMap<>();

        dtos.forEach(dto -> stockIdToMetricsDTOs.computeIfAbsent(dto.getStockId(), k -> new ArrayList<>()).add(dto));

        return stockIdToMetricsDTOs;
    }
}
