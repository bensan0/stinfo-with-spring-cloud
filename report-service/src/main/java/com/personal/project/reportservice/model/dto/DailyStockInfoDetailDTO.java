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

        //5日上穿月線, rsi黃金交叉etc...
        private List<String> extraTags;
    }
}
