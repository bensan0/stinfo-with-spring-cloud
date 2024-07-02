package com.personal.project.stockservice.model.entity;

import lombok.Data;

import java.util.List;

@Data
public class Tags {

    //漲rise
    //跌fall
    //平unchanged

    //價格連Ｎ漲/跌/平, ex: 價格連2漲 ["2", "rise"]
    private String[] consecutivePrice;

    //交易量連Ｎ漲/跌
    private String[] consecutiveTradingVolume;

    //交易額連Ｎ漲/跌
    private String[] consecutiveTradingAmount;

    //狀態：漲/跌/平/原 to 現
    private String priceStatus;

    //對比3天前價格漲/跌/平 XX% ex 價格持平 ["unchanged", "0"]
    private String[] priceVS2DaysAgo;

    //對比3天前交易量漲/跌 XX%
    private String[] tradingVolumeVS2DaysAgo;

    //對比3天前交易額漲/跌 XX%
    private String[] tradingAmountVS2DaysAgo;

    //對比5天前價格漲/跌 XX%
    private String[] priceVS4DaysAgo;

    //對比5天前交易額漲/跌 XX%
    private String[] tradingVolumeVS4DaysAgo;

    private String[] tradingAmountVS4DaysAgo;

    //5日上穿月線, rsi黃金交叉etc...
    private List<String> extraTags;
}
