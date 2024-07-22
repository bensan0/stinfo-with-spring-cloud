package com.personal.project.authservice.exception;



public class UserNotFoundException extends CustomException {

    private String message = "User not found";

    private String extraData = "";

    public UserNotFoundException() {
    }

    public UserNotFoundException(String msg) {
        super(msg);
        this.message = msg;
    }

    public UserNotFoundException(String msg, String extraData) {
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
