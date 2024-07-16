package com.personal.project.commoncore.constants;

import java.util.Arrays;

public enum ResponseCode {


    Success("200", null),
    Not_Valid("403", "not valid"),
    Not_Found("404", "data not found"),
    Invalid_Args("405", "params invalid"),
    Failed("500", "server error");

    private String code;
    private String msg;

    ResponseCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }

    public static ResponseCode valueOfCode(String code) {
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
