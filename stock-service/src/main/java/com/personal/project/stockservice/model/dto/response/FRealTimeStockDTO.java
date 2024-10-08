package com.personal.project.stockservice.model.dto.response;

import lombok.Data;

@Data
public class FRealTimeStockDTO {


	private String stockId;

	private String stockName;

	private String todayClosingPrice;

	private String priceGap;

	private String priceGapPercent;

	private String todayTradingVolumePiece;

	private String tags;

	private String link;

	private String market;
}
