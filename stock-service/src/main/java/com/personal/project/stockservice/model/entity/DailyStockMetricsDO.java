package com.personal.project.stockservice.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName(value = "daily_stock_metrics")
public class DailyStockMetricsDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String stockId;

    private String stockName;

    //當日收盤價, 用以判斷當日是否為交易日, 避免還需要以join daily_stock_info來做判斷
    private BigDecimal todayClosingPrice;

    // yyyyMMdd
    private Long date;

    // 5日均
    @TableField(value = "ma_5")
    private BigDecimal ma5;

    @TableField(value = "last_ma_5_price")
    private BigDecimal lastMA5price;// 5日最舊價格

    // 10日均
    @TableField(value = "ma_10")
    private BigDecimal ma10;

    @TableField(value = "last_ma_10_price")
    private BigDecimal lastMA10price;

    // 月線
    @TableField(value = "ma_20")
    private BigDecimal ma20;

    @TableField(value = "last_ma_20_price")
    private BigDecimal lastMA20price;

    // 季線
    @TableField(value = "ma_60")
    private BigDecimal ma60;

    @TableField(value = "last_ma_60_price")
    private BigDecimal lastMA60price;

    // 半年線
    @TableField(value = "ma_120")
    private BigDecimal ma120;

    @TableField(value = "last_ma_120_price")
    private BigDecimal lastMA120price;

    // 年線
    @TableField(value = "ma_240")
    private BigDecimal ma240;

    @TableField(value = "last_ma_240_price")
    private BigDecimal lastMA240price;
}
