package com.personal.project.stockservice.model.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class QueryConditionDTO {

    private Long tradingVolumePieceStart;

    private Long tradingVolumePieceEnd;

    private BigDecimal priceGapPercent;

    //價格連漲/跌/平
    private String conPriceDays;
    private String conPriceStatus;

    //交易量連漲/跌/平
    private String conVolDays;
    private String tradingVolumeStatus;

    //交易額連漲/跌/平
    private String conAmountDays;
    private String tradingAmountStatus;

    private List<String> extraTags;
}
