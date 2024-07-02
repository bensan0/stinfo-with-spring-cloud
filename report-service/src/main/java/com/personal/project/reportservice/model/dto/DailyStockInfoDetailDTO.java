package com.personal.project.reportservice.model.dto;

import cn.hutool.json.JSONUtil;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DailyStockInfoDetailDTO {

    private Long id;

    private String stockId;

    private Long date;

    private BigDecimal todayClosingPrice;

    private BigDecimal upperShadow;

    private BigDecimal lowerShadow;

    private BigDecimal realBody;

    private TagsDTO tags;

    @Data
    public static class TagsDTO {
        //漲rise
        //跌fall
        //平unchanged

        //價格連Ｎ漲/跌/平, ex: 價格連2漲 ["2", "漲"]
        private String[] consecutivePrice;

        //交易量連Ｎ漲/跌/平
        private String[] consecutiveTradingVolume;

        //交易額連Ｎ漲/跌/平
        private String[] consecutiveTradingAmount;

        //狀態：昨->現 ex:漲->跌
        private String priceStatus;

        //對比3天前價格漲/跌 XX%
        private String[] priceVS2DaysAgo;

        //對比3天前交易量漲/跌 XX%
        private String[] tradingVolumeVS2DaysAgo;

        //對比3天前交易額漲/跌 XX%
        private String[] tradingAmountVS2DaysAgo;

        //對比5天前價格漲/跌 XX%
        private String[] priceVS4DaysAgo;

        //對比5天前交易量漲/跌 XX%
        private String[] tradingVolumeVS4DaysAgo;

        //對比5天前交易額漲/跌 XX%
        private String[] tradingAmountVS4DaysAgo;

        //5日上穿月線, rsi黃金交叉etc...
        private List<String> extraTags;
    }
}
