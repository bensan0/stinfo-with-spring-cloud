CREATE database if NOT EXISTS `gateway` default character set utf8mb4 collate utf8mb4_unicode_ci;
use `gateway`;

SET NAMES utf8mb4;

create table if not exists connection_info
(
    id                     bigint unsigned auto_increment                        primary key,
    ip                     varchar(30)                      default ''        not null comment 'ip',
    path                   varchar(255)                     default ''        not null comment '連線路徑',
    date                   varchar(14)                      default ''        not null comment '連線時間'
 )
    comment '連入者資訊' charset = utf8mb4;