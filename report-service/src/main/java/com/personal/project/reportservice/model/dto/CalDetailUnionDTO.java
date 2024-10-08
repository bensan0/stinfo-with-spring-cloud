package com.personal.project.reportservice.model.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CalDetailUnionDTO {

    private Map<String, List<DailyStockInfoDTO>> stockIdToInfos;

    private Map<String, List<DailyStockMetricsDTO>> stockIdToMetrics;

    private Map<String, List<DailyStockInfoDetailDTO>> stockIdToDetails;
}
