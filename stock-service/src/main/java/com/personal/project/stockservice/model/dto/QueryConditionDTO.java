package com.personal.project.stockservice.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class QueryConditionDTO {

    private String date;

    private BigDecimal priceStart;

    private BigDecimal priceEnd;

    private Long tradingVolumePieceStart;

    private Long tradingVolumePieceEnd;

    //價格連漲/跌/平
    private String priceSelection;

    //交易量連漲/跌/平
    private String tradingVolumeSelection;

    //交易額連漲/跌/平
    private String tradingAmountSelection;

    //狀態：漲/跌/平/翻紅/翻綠
    private String priceStatusSelection;

    private List<String> extraTagsSelections;
}
