CREATE database if NOT EXISTS `scraper_service` default character set utf8mb4 collate utf8mb4_unicode_ci;

use scraper_service;

SET NAMES utf8mb4;

create table if not exists scraper_error_message
(
    id                              int unsigned auto_increment                      primary key,
    date                            varchar(30)                             not null comment 'yyyy-MM-ddTHH:mm:ss.SSSSSS',
    scraper_name                    varchar(30)                  default '' not null comment '爬蟲名稱',
    error_message                   varchar(500)                            null     comment '錯誤訊息',
    exception                       varchar(255)                            null     comment '錯誤名稱',
    exception_message               varchar(500)                            null     comment '例外原因訊息',
    extra                           varchar(500)                            null     comment '額外資訊',
    status                          tinyint unsigned             default 0  not null comment '0-未處理, 1-已處理, 2-忽略(不予處理)'
    )
    comment '爬蟲時錯誤' charset = utf8mb4;

