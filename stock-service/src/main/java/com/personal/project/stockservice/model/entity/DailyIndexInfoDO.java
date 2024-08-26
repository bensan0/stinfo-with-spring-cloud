package com.personal.project.stockservice.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("daily_index_info")
public class DailyIndexInfoDO {

	@TableId(type = IdType.AUTO)
	private Long id;

	private String indexName;

	private Long date;

	private BigDecimal todayClosing;

	private BigDecimal yesterdayClosing;

	private BigDecimal gap;

	private BigDecimal gapPercent;

	private BigDecimal opening;

	private BigDecimal highest;

	private BigDecimal lowest;

	private Long todayTradingVolume;

	private BigDecimal todayTradingAmount;

	@TableField(fill = FieldFill.INSERT_UPDATE)
	private Long updatedAt;
}
