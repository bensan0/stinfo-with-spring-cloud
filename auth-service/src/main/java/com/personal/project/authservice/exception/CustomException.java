package com.personal.project.authservice.exception;

public abstract class CustomException extends RuntimeException{

    public CustomException(){
        super();
    }

    public CustomException(String msg){
        super(msg);
    }

    public CustomException(String msg, String extraData){
        super(msg);
    }

    public abstract String getDefaultMessage();

    public abstract String getExtraData();
}
