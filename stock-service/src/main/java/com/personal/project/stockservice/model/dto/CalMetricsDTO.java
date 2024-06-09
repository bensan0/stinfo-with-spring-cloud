package com.personal.project.stockservice.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CalMetricsDTO {

    private String stockId;
    private String stockName;
    private Long date;
    private BigDecimal todayClosingPrice;
    private BigDecimal rsi3;
    private BigDecimal rsi5;
    private BigDecimal rsi6;
    private BigDecimal rsi10;
    private BigDecimal rsi12;
    private BigDecimal ma5;
    private BigDecimal lastMA5Price;
    private BigDecimal ma10;
    private BigDecimal lastMA10Price;
    private BigDecimal ma20;
    private BigDecimal lastMA20Price;
    private BigDecimal ma60;
    private BigDecimal lastMA60Price;
    private BigDecimal ma120;
    private BigDecimal lastMA120Price;
    private BigDecimal ma240;
    private BigDecimal lastMA240Price;
}
