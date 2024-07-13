package com.personal.project.stockservice.model.dto;

import com.personal.project.stockservice.model.entity.Tags;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DailyStockInfoDetailDTO {

    private Long id;

    private String stockId;

    private Long date;

    private BigDecimal todayClosingPrice;

    private BigDecimal upperShadow;

    private BigDecimal lowerShadow;

    private BigDecimal realBody;

    private Tags tags;
}
