package com.personal.project.stockservice.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockInfo4CalDetailDTO {

    private String stockId;

    private String stockName;

    // yyyyMMdd
    private Long date;

    private BigDecimal todayClosingPrice;

    private BigDecimal yesterdayClosingPrice;

    private BigDecimal openingPrice;

    private BigDecimal highestPrice;

    private BigDecimal lowestPrice;

    private Long todayTradingVolumePiece;

    private Long yesterdayTradingVolumePiece;

    private BigDecimal todayTradingVolumeMoney;

    private BigDecimal yesterdayTradingVolumeMoney;

    private Integer sequence;
}
