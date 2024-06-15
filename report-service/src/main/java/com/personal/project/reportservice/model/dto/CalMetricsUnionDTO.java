package com.personal.project.reportservice.model.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CalMetricsUnionDTO {

    public CalMetricsUnionDTO(Map<String, List<DailyStockMetricsDTO>> stockIdToMetrics, Map<String, List<DailyStockInfoDTO>> stockIdToInfos) {
        this.stockIdToMetrics = stockIdToMetrics;
        this.stockIdToInfos = stockIdToInfos;
    }

    private Map<String, List<DailyStockMetricsDTO>> stockIdToMetrics;
    private Map<String, List<DailyStockInfoDTO>> stockIdToInfos;
}
