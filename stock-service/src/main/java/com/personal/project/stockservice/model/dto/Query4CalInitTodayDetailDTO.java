package com.personal.project.stockservice.model.dto;

import java.util.List;
import java.util.Map;

public record Query4CalInitTodayDetailDTO(
        Map<String, List<DailyStockInfoDTO>> stockIdToInfoDTOs,
        Map<String, List<DailyStockMetricsDTO>> stockIdToMetricsDTOs,
        Map<String, DailyStockInfoDetailDTO> stockIdToDetailDTO
) {
}
