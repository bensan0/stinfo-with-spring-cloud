package com.personal.project.stockservice.model.dto.response;

import lombok.Data;

@Data
public class RealTimeStockDTO {
	private String stockId;

	private String stockName;

	private String todayClosingPrice;

	private String priceGap;

	private String priceGapPercent;

	private String todayTradingVolumePiece;

	private String market;

	private String tags;
}
