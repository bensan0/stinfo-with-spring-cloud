package com.personal.project.stockservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personal.project.stockservice.model.dto.CalMetricsDTO;
import com.personal.project.stockservice.model.dto.DailyStockMetricsDTO;
import com.personal.project.stockservice.model.dto.ManualCalDTO;
import com.personal.project.stockservice.model.dto.Query4CalDTO;
import com.personal.project.stockservice.model.entity.DailyStockMetricsDO;

import java.util.List;

public interface DailyStockMetricsMapper extends BaseMapper<DailyStockMetricsDO> {

    List<CalMetricsDTO> query4CalMetrics(Query4CalDTO query4CalDto);

    List<CalMetricsDTO> query4CalMetricsManual(ManualCalDTO manualCalDTO);

    List<DailyStockMetricsDTO> query4CalDetail(Query4CalDTO query4CalDto);

    List<DailyStockMetricsDTO> query4ManualCalDetail(ManualCalDTO manualCalDTO);

}
