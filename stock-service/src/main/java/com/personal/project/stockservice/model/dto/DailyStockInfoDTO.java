package com.personal.project.stockservice.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DailyStockInfoDTO {

    private String stockId;

    private String stockName;

    // yyyyMMdd
    private Long date;

    private BigDecimal todayClosingPrice;

    private BigDecimal yesterdayClosingPrice;

    private BigDecimal priceGap;

    private BigDecimal priceGapPercent;

    private BigDecimal openingPrice;

    private BigDecimal highestPrice;

    private BigDecimal lowestPrice;

    private Long todayTradingVolumePiece;

    private BigDecimal todayTradingVolumeMoney;

    private Long yesterdayTradingVolumePiece;

    private BigDecimal yesterdayTradingVolumeMoney;
}
