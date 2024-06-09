package com.personal.project.reportservice.constant;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public enum StatusEnum {
    UNHANDLED(0),
    HANDLED(1),
    SKIP(2)//不予處理
    ;

    final Integer code;

    StatusEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return this.code;
    }

    public static Optional<StatusEnum> of(Integer code) {

        return Arrays.stream(StatusEnum.values()).filter(e -> Objects.equals(e.getCode(), code)).findFirst();
    }
}
