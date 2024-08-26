package com.personal.project.stockservice.model.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DailyIndexInfoDTO {

	private Long id;

	private String indexName;

	private Long date;

	private BigDecimal todayClosing;

	private BigDecimal yesterdayClosing;

	private BigDecimal gap;

	private BigDecimal gapPercent;

	private BigDecimal opening;

	private BigDecimal highest;

	private BigDecimal lowest;

	private Long todayTradingVolume;

	private BigDecimal todayTradingAmount;
}
