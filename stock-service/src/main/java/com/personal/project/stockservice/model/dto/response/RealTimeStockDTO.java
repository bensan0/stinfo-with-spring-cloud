package com.personal.project.stockservice.model.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RealTimeStockDTO {
	private String stockId;

	private String stockName;

	private String todayClosingPrice;

	private String priceGap;

	private String priceGapPercent;

	private String todayTradingVolumePiece;

	private String yesterdayTradingVolumePiece;

	private BigDecimal upperShadow;

	private BigDecimal realBody;

	private BigDecimal lowerShadow;

	private String market;

	private String tags;
}
