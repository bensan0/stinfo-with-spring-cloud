package com.personal.project.userservice.exceptionhandler.exception;

import com.personal.project.commoncore.exception.CustomException;

public class WrongArgsException extends CustomException {

    private String message = "Wrong arguments";

    private String extraData = "";

    public WrongArgsException() {
    }

    public WrongArgsException(String msg) {
        super(msg);
        this.message = msg;
    }

    public WrongArgsException(String msg, String extraData) {
        super(msg);
        this.message = msg;
        this.extraData = extraData;
    }

    @Override
    public String getDefaultMessage() {
        return message;
    }

    @Override
    public String getExtraData() {
        return extraData;
    }
}
