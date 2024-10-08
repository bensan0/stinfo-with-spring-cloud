package com.personal.project.stockservice.model.dto.response;

import java.util.List;
import java.util.Map;

public record Query4CalInitTodayMetricsDTO(Map<String, List<StockInfo4InitMetricsDTO>> stockIdToInfoDTOs,
                                           Map<String, DailyStockMetricsDTO> stockIdToMetricsDTO) {

}
