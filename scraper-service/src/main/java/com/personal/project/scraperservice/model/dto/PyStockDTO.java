package com.personal.project.scraperservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PyStockDTO {

	@JsonProperty("stock_id")
	private String stockId;

	@JsonProperty("stock_name")
	private String stockName;

	private String market;

	private Long date;

	@JsonProperty("today_closing_price")
	private BigDecimal todayClosingPrice;

	@JsonProperty("yesterday_closing_price")
	private BigDecimal yesterdayClosingPrice;

	@JsonProperty("price_gap")
	private BigDecimal priceGap;

	@JsonProperty("price_gap_percent")
	private BigDecimal priceGapPercent;

	@JsonProperty("opening_price")
	private BigDecimal openingPrice;

	@JsonProperty("highest_price")
	private BigDecimal highestPrice;

	@JsonProperty("lowest_price")
	private BigDecimal lowestPrice;

	@JsonProperty("today_trading_volume_piece")
	private Long todayTradingVolumePiece;

	@JsonProperty("today_trading_volume_money")
	private BigDecimal todayTradingVolumeMoney;
}
