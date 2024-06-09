package com.personal.project.stockservice.model.dto;

import java.util.List;

public record CalMetricsUnionDTO(List<CalMetricsDTO> calMetricsInfo,
                                 List<PastClosingPriceDTO> pastClosingPrice,
                                 List<SimpleMetricsDTO> todayExistedMetrics) {
}
