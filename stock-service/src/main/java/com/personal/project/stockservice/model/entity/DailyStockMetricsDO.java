package com.personal.project.stockservice.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("daily_stock_metrics")
public class DailyStockMetricsDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String stockId;

    private String stockName;

    //當日收盤價, 用以判斷當日是否為交易日, 避免還需要以join daily_stock_info來做判斷
    private BigDecimal todayClosingPrice;

    // yyyyMMdd
    private Long date;

    private BigDecimal rsi3;

    private BigDecimal rsi5;

    private BigDecimal rsi6;

    private BigDecimal rsi10;

    private BigDecimal rsi12;

    // 5日均
    private BigDecimal ma5;
    private BigDecimal lastMA5price;// 5日最舊價格

    // 10日均
    private BigDecimal ma10;
    private BigDecimal lastMA10price;

    // 月線
    private BigDecimal ma20;
    private BigDecimal lastMA20price;

    // 季線
    private BigDecimal ma60;
    private BigDecimal lastMA60price;

    // 半年線
    private BigDecimal ma120;
    private BigDecimal lastMA120price;

    // 年線
    private BigDecimal ma240;
    private BigDecimal lastMA240price;
}
