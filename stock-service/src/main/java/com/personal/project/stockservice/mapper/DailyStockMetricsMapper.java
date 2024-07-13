package com.personal.project.stockservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personal.project.stockservice.model.dto.DailyStockMetricsDTO;
import com.personal.project.stockservice.model.dto.ManualCalDTO;
import com.personal.project.stockservice.model.dto.Query4CalDTO;
import com.personal.project.stockservice.model.entity.DailyStockMetricsDO;

import java.util.List;

public interface DailyStockMetricsMapper extends BaseMapper<DailyStockMetricsDO> {

    List<DailyStockMetricsDTO> query4CalMetrics(Long date);

    List<DailyStockMetricsDTO> query4CalDetail(Long date);

    List<DailyStockMetricsDTO> query4ManualCalDetail(ManualCalDTO manualCalDTO);

    List<DailyStockMetricsDTO> query4CalInitTodayMetrics();

    List<DailyStockMetricsDTO> query4CalInitTodayDetail();
}
