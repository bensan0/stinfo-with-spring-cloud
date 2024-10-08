package com.personal.project.scraperservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PyIndexDTO {

	@JsonProperty("index_name")
	private String indexName;

	private Long date;

	@JsonProperty("today_closing")
	private BigDecimal todayClosing;

	@JsonProperty("yesterday_closing")
	private BigDecimal yesterdayClosing;

	private BigDecimal gap;

	@JsonProperty("gap_percent")
	private BigDecimal gapPercent;

	private BigDecimal opening;

	private BigDecimal highest;

	private BigDecimal lowest;

	@JsonProperty("today_trading_volume")
	private BigDecimal todayTradingVolume;

	@JsonProperty("today_trading_amount")
	private BigDecimal todayTradingAmount;
}
