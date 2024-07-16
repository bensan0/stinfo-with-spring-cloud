package com.personal.project.stockservice.model.dto.response;

import java.util.List;
import java.util.Map;

public record CalMetricsUnionDTO(Map<String, List<DailyStockMetricsDTO>> stockIdToMetrics,
                                 Map<String, List<DailyStockInfoDTO>> stockIdToInfos) {
}
