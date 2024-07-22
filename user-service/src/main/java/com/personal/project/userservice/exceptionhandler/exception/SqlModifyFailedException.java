package com.personal.project.userservice.exceptionhandler.exception;



public class SqlModifyFailedException extends CustomException {


    private String message = "sql modify failed";

    private String extraData;

    public SqlModifyFailedException() {
    }

    public SqlModifyFailedException(String msg) {
        super(msg);
        this.message = msg;
    }

    public SqlModifyFailedException(String msg, String extraData) {
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
