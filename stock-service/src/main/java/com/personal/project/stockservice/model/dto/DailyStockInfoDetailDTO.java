package com.personal.project.stockservice.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DailyStockInfoDetailDTO {

    private String stockId;

    private Long date;

    private BigDecimal upperShadow;

    private BigDecimal lowerShadow;

    private BigDecimal realBody;

    private String tags;
}
