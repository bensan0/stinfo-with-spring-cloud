package com.personal.project.reportservice.model.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Query4CalInitTodayDetailDTO {

    private Map<String, List<DailyStockInfoDTO>> stockIdToInfoDTOs;

    private Map<String, List<DailyStockMetricsDTO>> stockIdToMetricsDTOs;

    private Map<String, DailyStockInfoDetailDTO> stockIdToDetailDTO;
}
