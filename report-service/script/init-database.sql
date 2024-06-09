CREATE database if NOT EXISTS `report_servie` default character set utf8mb4 collate utf8mb4_unicode_ci;
use `stock_servie`;

SET NAMES utf8mb4;

create table if not exists report_error_message
(
    id                              int unsigned auto_increment                      primary key,
    date                            varchar(30)                             not null comment 'yyyy-MM-ddTHH:mm:ss.SSSSSS',
    reportName                      varchar(30)                  default '' not null comment '報告名稱',
    errorMessage                    varchar(500)                            null     comment '錯誤訊息',
    exception                       varchar(255)                            null     comment '錯誤名稱',
    exceptionMessage                varchar(500)                            null     comment '例外原因訊息',
    extra                           varchar(500)                            null     comment '額外資訊',
    status                          tinyint unsigned             default 0  not null comment '0-未處理, 1-已處理, 2-忽略(不予處理)'
    )
    comment '報告生成時錯誤' charset = utf8mb4;