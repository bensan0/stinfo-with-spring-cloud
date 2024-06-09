package com.personal.project.reportservice.constant;

import lombok.Getter;

@Getter
public enum DetailTagEnum {

    //todo 趨勢

    //todo trading volume

    //todo trading amount

    //MA
    MA5_UP_THROUGH_MA20("5日線上穿月線"),
    MA20_UP_THROUGH_MA5("月線上穿5日線"),
    MA20_UP_THROUGH_MA60("月線上穿季線"),
    MA5_DOWN_THROUGH_MA20("5日線下穿月線"),
    MA20_DOWN_THROUGH_MA60("月線下穿季線"),
    MA_QUEUED_UP("均線多頭排列"),

    //price
    PRICE_OVER_MA5("價格5日線之上"),
    PRICE_OVER_MA20("價格月線之上"),
    PRICE_OVER_MA60("價格季線之上"),

    //cross
    JUMP_UP_LIMIT("一字跳空"),
    JUMP_DOWM_LIMIT("一字跳水"),
    T("T字"),
    STRONGER_CROSS("強十字"),
    STRONG_CROSS("偏強十字"),
    CROSS("正十字線"),
    WEAK_CROSS("偏弱十字"),
    WEAKER_CROSS("弱十字"),
    GRAVE("墓碑"),

    //lower shadow
    HAVE_LOWER_SHADOW("有下影線"),
    TESTING_MA20_SUPPORT("回測月線支撐"),
    TESTING_MA5_SUPPORT("回測5日線支撐"),
    TESTING_MA60_SUPPORT("回測季線支撐"),
    NO_LOWER_SHADOW("無下影線"),
    STRONG_SUPPORT("強支撐"),

    //upper shadow
    HAVE_UPPER_SHADOW("有上影線"),
    NO_UPPER_SHADOW("無上影線"),
    TESTING_MA20_PRESSURE("測月線壓力"),
    TESTING_MA5_PRESSURE("測5日壓力"),
    STRONG_PRESSURE("強壓力"),

    //K stick
    GAP_UP("跳空"),
    GAP_DOWN("跳水"),
    RED("紅"),
    GREEN("綠"),
    RED_HAMMER("紅錘子"),
    GREEN_HAMMER("綠錘子"),
    LONG_RED("長紅"),
    MAX_RED("紅滿"),
    MAX_GREEN("綠滿"),
    LONG_GREEN("長綠"),
    RED_INVERTED_HAMMER("紅倒錘子"),
    GREEN_INVERTED_HAMMER("綠倒錘子"),
    ;


    final String tag;

    DetailTagEnum(String tag) {
        this.tag = tag;
    }
}
