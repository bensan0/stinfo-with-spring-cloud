package com.personal.project.scraperservice.constant;

public enum Term {
    //股票編號
    STOCK_ID("stockId"),
    STOCK_NAME("stockName"),
    DATE("date"),
    //今日收盤價
    TODAY_CLOSING_PRICE("todayClosingPrice"),
    //昨日收盤價
    YESTERDAY_CLOSING_PRICE("yesterdayClosingPrice"),
    //價差
    PRICE_GAP("priceGap"),
    //漲跌幅
    PRICE_GAP_PERCENT("priceGapPercent"),
    //本日開盤價
    OPENING_PRICE("openingPrice"),
    //本日最高價
    HIGHEST_PRICE("highestPrice"),
    //本日最低價
    LOWEST_PRICE("lowestPrice"),
    //本日成交張數
    TODAY_TRADING_VOLUME_PIECE("todayTradingVolumePiece"),
    //本日成交金額
    TODAY_TRADING_VOLUME_MONEY("todayTradingVolumeMoney"),
    //昨日成交張數
    YESTERDAY_TRADING_VOLUME_PIECE("yesterdayTradingVolumePiece"),
    //昨日成交金額
    YESTERDAY_TRADING_VOLUME_MONEY("yesterdayTradingVolumeMoney"),
    //市場
    OTC("櫃"),
    LISTED("市"),
    ;

    private final String fieldName;

    Term(String fieldName){
        this.fieldName = fieldName;
    }

    public String getFieldName(){
        return this.fieldName;
    }
}
