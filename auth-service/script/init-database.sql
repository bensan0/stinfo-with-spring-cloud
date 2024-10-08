CREATE database if NOT EXISTS `auth_service` default character set utf8mb4 collate utf8mb4_unicode_ci;
use `auth_service`;

SET NAMES utf8mb4;

create table if not exists plat_user
(
    id                     int unsigned auto_increment                       primary key,
    username               varchar(20)                      default '0'       not null comment '帳號',
    password               varchar(32)                      default ''        not null comment '密碼',
    salt                   varchar(16)                       default ''        not null comment '鹽值',
    deleted                tinyint unsigned                 default 0         not null comment '軟刪除',
    created_at             bigint unsigned                                        null comment '創建日期',
    updated_at             bigint unsigned                                        null comment '更新日期',
    status                 tinyint unsigned                 default 0         not null comment '0-審核中,1-正常,2-禁止'
 )
    comment '用戶' charset = utf8mb4;

 create table if not exists plat_role
 (
     id                     int unsigned auto_increment                       primary key,
     name                   varchar(10)                     default 'unknown' not null comment '角色名'
 )
     comment '角色' charset = utf8mb4;

 create table if not exists plat_permission
 (
     id                     int unsigned auto_increment                       primary key,
     resource               varchar(100)                    default 'unknown' not null comment '資源'
 )
     comment '權限' charset = utf8mb4;