CREATE database if NOT EXISTS `stock_service` default character set utf8mb4 collate utf8mb4_unicode_ci;
use `stock_service`;

SET NAMES utf8mb4;

create table if not exists daily_stock_info
(
    id                              int unsigned auto_increment                       primary key,
    stock_id                        varchar(6)                      default '0'       not null comment '股票代號',
    stock_name                      varchar(20)                     default ''        not null comment '股名',
    market                          varchar(3)                      default ''        not null comment '市場',
    date                            bigint unsigned                 default 0         not null comment 'yyyyMMdd',
    today_closing_price             decimal(7,2) unsigned                             null     comment '今日收盤價',
    yesterday_closing_price         decimal(7,2) unsigned                             null     comment '昨日收盤價',
    price_gap                       decimal(7,2)                                      null     comment '價差(今日收盤-昨日收盤)',
    price_gap_percent               decimal(5,2)                                      null     comment '價差百分比(價差/昨收)',
    opening_price                   decimal(7,2) unsigned                             null     comment '開盤價',
    highest_price                   decimal(7,2) unsigned                             null     comment '盤中最高價',
    lowest_price                    decimal(7,2) unsigned                             null     comment '盤中最低價',
    today_trading_volume_piece      bigint unsigned                                   null     comment '今日交易量(張)',
    today_trading_volume_money      decimal(14,2) unsigned                            null     comment '今日交易量(元)',
    yesterday_trading_volume_piece  bigint unsigned                                   null     comment '昨日交易量(張)',
    yesterday_trading_volume_money  decimal(14,2) unsigned                            null     comment '昨日交易量(元)',
    updated_at                      bigint unsigned                 default 0         not null comment 'yyyyMMddHHmmss'
    )
    comment '每日股票概要' charset = utf8mb4;
    create index idx_daily_stock_info_stockid on daily_stock_info (stock_id);

create table if not exists daily_stock_metrics
(
    id                              int unsigned auto_increment                       primary key,
    stock_id                        varchar(6)                      default '0'       not null comment '股票代號',
    stock_name                      varchar(20)                     default ''        not null comment '股名',
    today_closing_price             decimal(7,2) unsigned                             null     comment '今日收盤價',
    date                            bigint unsigned                 default 0         not null comment 'yyyyMMdd',
    ma_5                            decimal(7,2) unsigned                             null     comment '5日均價',
    last_ma_5_price                 decimal(7,2) unsigned                             null     comment '用以計算本次均價的最舊一次收盤價',
    ma_10                           decimal(7,2) unsigned                             null     comment '10日均價',
    last_ma_10_price                decimal(7,2) unsigned                             null     comment '用以計算本次均價的最舊一次收盤價',
    ma_20                           decimal(7,2) unsigned                             null     comment '20日均價',
    last_ma_20_price                decimal(7,2) unsigned                             null     comment '用以計算本次均價的最舊一次收盤價',
    ma_60                           decimal(7,2) unsigned                             null     comment '60日均價',
    last_ma_60_price                decimal(7,2) unsigned                             null     comment '用以計算本次均價的最舊一次收盤價',
    ma_120                          decimal(7,2) unsigned                             null     comment '120日均價',
    last_ma_120_price               decimal(7,2) unsigned                             null     comment '用以計算本次均價的最舊一次收盤價',
    ma_240                          decimal(7,2) unsigned                             null     comment '240日均價',
    last_ma_240_price               decimal(7,2) unsigned                             null     comment '用以計算本次均價的最舊一次收盤價',
    updated_at                      bigint       unsigned           default 0         not null comment 'yyyyMMddHHmmss'
    )
    comment '每日股票指標數據' charset = utf8mb4;
    create index idx_daily_stock_metrics_stockid on daily_stock_metrics (stock_id);

create table if not exists daily_stock_info_detail
(
    id                              int unsigned auto_increment                       primary key,
    stock_id                        varchar(6)                      default '0'       not null comment '股票代號',
    date                            bigint unsigned                 default 0         not null comment 'yyyyMMdd',
    today_closing_price             decimal(7,2) unsigned                             null     comment '今日收盤價',
    upper_shadow                    decimal(5,2) unsigned                             null     comment '上影線佔長(%)',
    lower_shadow                    decimal(5,2) unsigned                             null     comment '下影線佔長(%)',
    real_body                       decimal(5,2) unsigned                             null     comment '實體佔長(%)',
    tags                            json                                              null     comment 'json, 標籤詳細資訊',
    updated_at                      bigint unsigned                 default 0         not null comment 'yyyyMMddHHmmss'
    )
    comment '每日股票標籤詳細數據' charset = utf8mb4;
    create index idx_daily_stock_detail_stockid on daily_stock_detail (stock_id);