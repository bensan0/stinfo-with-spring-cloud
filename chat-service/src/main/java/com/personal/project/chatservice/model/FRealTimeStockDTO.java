package com.personal.project.chatservice.model;

import lombok.Data;

import java.util.List;

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

	@Data
	public static class ExtraTags{
		List<String> extraTags;
	}
}
