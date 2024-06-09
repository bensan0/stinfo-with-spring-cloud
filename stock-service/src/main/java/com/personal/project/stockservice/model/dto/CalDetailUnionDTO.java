package com.personal.project.stockservice.model.dto;

import java.util.List;


public record CalDetailUnionDTO(
        List<StockInfo4CalDetailDTO> stockInfo4CalDetailDTOS,
        List<DailyStockMetricsDTO> dailyStockMetricsDTOs,
        List<DailyStockInfoDetailDTO> dailyStockInfoDetailDTOs,
        List<SimpleDetailDTO> todayExistedDetail
) {
}
