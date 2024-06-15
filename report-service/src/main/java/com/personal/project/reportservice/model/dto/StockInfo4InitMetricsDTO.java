package com.personal.project.reportservice.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockInfo4InitMetricsDTO {

    private String stockId;

    private String stockName;

    // yyyyMMdd
    private Long date;

    private BigDecimal todayClosingPrice;
}
