package com.personal.project.stockservice.model.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class QueryConditionRealTimeDTO {

	private Long tradingVolumePieceStart;

	private BigDecimal priceGapPercent;

	private List<String> extraTags;
}
