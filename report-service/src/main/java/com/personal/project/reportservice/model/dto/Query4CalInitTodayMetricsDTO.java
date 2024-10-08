package com.personal.project.reportservice.model.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Query4CalInitTodayMetricsDTO {

    private Map<String, List<StockInfo4InitMetricsDTO>> stockIdToInfoDTOs;

    private Map<String, DailyStockMetricsDTO> stockIdToMetricsDTO;
}
