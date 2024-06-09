package com.personal.project.stockservice.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.personal.project.stockservice.typehandler.TagsHandler;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@TableName(value = "daily_stock_info_detail", autoResultMap = true)
@Data
@ToString
public class DailyStockInfoDetailDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String stockId;

    //yyMMdd
    private Long date;

    private BigDecimal todayClosingPrice;

    //上影線佔長(%)
    private BigDecimal upperShadow;

    //下影線佔長(%)
    private BigDecimal lowerShadow;

    //實體佔長(%)
    private BigDecimal realBody;

    @TableField(typeHandler = TagsHandler.class)
    private Tags tags;

    @Data
    @ToString
    public static class Tags {
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
}
