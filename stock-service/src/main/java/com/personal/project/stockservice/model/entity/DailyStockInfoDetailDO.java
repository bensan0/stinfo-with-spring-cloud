package com.personal.project.stockservice.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.personal.project.stockservice.typehandler.TagsHandler;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

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

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedAt;
}
