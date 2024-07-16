package com.personal.project.stockservice.model.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CompleteStockDTO {
	private String stockId;
	private String stockName;
	private Long date;
	private BigDecimal todayClosingPrice;
	private BigDecimal yesterdayClosingPrice;
	private BigDecimal priceGap;
	private BigDecimal priceGapPercent;
	private BigDecimal todayTradingVolumePiece;
	private BigDecimal todayTradingVolumeMoney;
	private BigDecimal upperShadow;
	private BigDecimal realBody;
	private BigDecimal lowerShadow;
	private BigDecimal ma5;
	private BigDecimal ma10;
	private BigDecimal ma20;
}
