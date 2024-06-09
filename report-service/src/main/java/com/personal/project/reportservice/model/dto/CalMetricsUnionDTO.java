package com.personal.project.reportservice.model.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CalMetricsUnionDTO {

    public CalMetricsUnionDTO(List<CalMetricsDTO> calMetricsInfo, List<PastClosingPriceDTO> pastClosingPrice, List<SimpleMetricsDTO> todayExistedMetrics) {
        this.calMetricsInfo = calMetricsInfo;
        this.pastClosingPrice = pastClosingPrice;
        this.todayExistedMetrics = todayExistedMetrics;
    }

    private List<CalMetricsDTO> calMetricsInfo;
    private List<PastClosingPriceDTO> pastClosingPrice;
    private List<SimpleMetricsDTO> todayExistedMetrics;
}
