package com.personal.project.scraperservice.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DailyStockInfoDto {

    private Long id;

    private String stockId;

    private String stockName;

    // yyyyMMdd
    private Long date;

    private BigDecimal todayClosingPrice;

    private BigDecimal yesterdayClosingPrice;

    private BigDecimal priceGap;

    private Double priceGapPercent;

    private BigDecimal openingPrice;

    private BigDecimal highestPrice;

    private BigDecimal lowestPrice;

    private Long todayTradingVolumePiece;

    private BigDecimal todayTradingVolumeMoney;

    private Long yesterdayTradingVolumePiece;

    private BigDecimal yesterdayTradingVolumeMoney;

}
