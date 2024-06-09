package com.personal.project.userservice.constant;

import lombok.Getter;

@Getter
public enum RoleEnum {

    Owner(1L, "最高權限", -2),
    Administrator(2L, "管理員", -1),
    Member(3L, "一般會員", 0)
    ;

    private Long index;

    private String desc;

    private Integer vip;

    RoleEnum(Long index, String desc, Integer vip) {
        this.index = index;
        this.desc = desc;
        this.vip = vip;
    }
}
