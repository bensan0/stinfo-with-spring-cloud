package com.personal.project.stockservice.model.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockInfo4InitDetailDTO {

    private String stockId;

    private String stockName;

    // yyyyMMdd
    private Long date;

    private BigDecimal openingPrice;

    private BigDecimal highestPrice;

    private BigDecimal lowestPrice;

    private BigDecimal todayClosingPrice;

    private BigDecimal todayTradingVolumePiece;

    private BigDecimal todayTradingVolumeMoney;
}
