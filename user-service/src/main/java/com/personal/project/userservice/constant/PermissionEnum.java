package com.personal.project.userservice.constant;

import lombok.Getter;

@Getter
public enum PermissionEnum {

    All("all", "任意通行"),
    ;

    private String feature;
    private String desc;

    PermissionEnum(String feature, String desc) {
        this.feature = feature;
        this.desc = desc;
    }
}
