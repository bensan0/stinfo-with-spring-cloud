package com.personal.project.reportservice.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PastClosingPriceDTO {

    private String stockId;

    private String stockName;

    // yyyyMMdd
    private Long date;

    private BigDecimal closingPrice;

    //均線種類
    private Integer ma;
}
