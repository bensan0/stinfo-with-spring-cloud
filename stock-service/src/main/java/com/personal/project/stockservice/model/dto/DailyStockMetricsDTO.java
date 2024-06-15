package com.personal.project.stockservice.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DailyStockMetricsDTO {

    private Long id;

    private String stockId;

    private String stockName;

    // yyyyMMdd
    private Long date;

    // 5日均
    private BigDecimal ma5;
    private BigDecimal lastMA5price;// 5日最舊價格

    // 10日均
    private BigDecimal ma10;
    private BigDecimal lastMA10price;

    // 月線
    private BigDecimal ma20;
    private BigDecimal lastMA20price;

    // 季線
    private BigDecimal ma60;
    private BigDecimal lastMA60price;

    // 半年線
    private BigDecimal ma120;
    private BigDecimal lastMA120price;

    // 年線
    private BigDecimal ma240;
    private BigDecimal lastMA240price;
}
