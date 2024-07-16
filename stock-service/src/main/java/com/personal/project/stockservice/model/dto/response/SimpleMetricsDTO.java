package com.personal.project.stockservice.model.dto.response;

import lombok.Data;

@Data
public class SimpleMetricsDTO {

    private Long metricsId;

    private String stockId;

    public SimpleMetricsDTO(Long metricsId, String stockId){
        this.metricsId = metricsId;
        this.stockId = stockId;
    }
}
