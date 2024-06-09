package com.personal.project.stockservice.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("daily_stock_info")
public class DailyStockInfoDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String stockId;

    private String stockName;

    // yyyyMMdd
    private Long date;

    private BigDecimal todayClosingPrice;

    private BigDecimal yesterdayClosingPrice;

    private BigDecimal priceGap;

    private BigDecimal priceGapPercent;

    private BigDecimal openingPrice;

    private BigDecimal highestPrice;

    private BigDecimal lowestPrice;

    private Long todayTradingVolumePiece;

    private BigDecimal todayTradingVolumeMoney;

    private Long yesterdayTradingVolumePiece;

    private BigDecimal yesterdayTradingVolumeMoney;

}
