package com.personal.project.stockservice.model.dto;

import com.personal.project.stockservice.model.entity.DailyStockInfoDetailDO;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ConditionQueryUnionInfoDTO {

    private String stockId;

    private String stockName;

    private Long date;

    private BigDecimal todayClosingPrice;

    private BigDecimal yesterdayClosingPrice;

    private BigDecimal priceGap;

    private BigDecimal priceGapPercent;

    private Long todayTradingVolumePiece;

    private BigDecimal todayTradingVolumeMoney;

    private Long yesterdayTradingVolumePiece;

    private BigDecimal yesterdayTradingVolumeMoney;

    private BigDecimal ma5;

    private BigDecimal ma10;

    private BigDecimal ma20;

    private BigDecimal ma60;

    private BigDecimal upperShadow;

    private BigDecimal lowerShadow;

    private BigDecimal realBody;

    private DailyStockInfoDetailDO.Tags tags;
}
