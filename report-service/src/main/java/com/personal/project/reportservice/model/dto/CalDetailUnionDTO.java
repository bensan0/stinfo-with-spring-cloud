package com.personal.project.reportservice.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class CalDetailUnionDTO {

    private List<StockInfo4CalDetailDTO> stockInfo4CalDetailDTOS;

    private List<DailyStockMetricsDTO> dailyStockMetricsDTOs;

    private List<SimpleDetailDTO> todayExistedDetailDTOs;

    private List<DailyStockInfoDetailDTO> dailyStockInfoDetailDTOs;
}
