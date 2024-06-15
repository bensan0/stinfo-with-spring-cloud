package com.personal.project.stockservice.model.dto;

import java.util.List;
import java.util.Map;


public record CalDetailUnionDTO(
        Map<String, List<DailyStockInfoDTO>> stockIdToInfos,
        Map<String,List<DailyStockMetricsDTO>> stockIdToMetrics,
        Map<String, List<DailyStockInfoDetailDTO>> stockIdToDetails
) {
}
