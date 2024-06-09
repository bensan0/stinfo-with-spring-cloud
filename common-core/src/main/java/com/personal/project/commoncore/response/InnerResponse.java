package com.personal.project.commoncore.response;

import com.personal.project.commoncore.constants.ResponseCode;

import java.io.Serializable;

public class InnerResponse<T> implements Serializable {

    private String code;

    private String msg;

    private T data;

    private int total;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> InnerResponse<T> ok(T data) {
        return restResult(data, ResponseCode.Success.getCode(), null);
    }

    public static <T> InnerResponse<T> failed(T data) {
        return restResult(data, ResponseCode.Failed.getCode(), null);
    }

    public static <T> InnerResponse<T> failed(String msg) {
        return restResult(null, ResponseCode.Failed.getCode(), msg);
    }

    public static <T> InnerResponse<T> failed(String code, String msg) {
        return restResult(null, code, msg);
    }

    private static <T> InnerResponse<T> restResult(T data, String code, String msg) {
        InnerResponse<T> apiResult = new InnerResponse<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMsg(msg);
        return apiResult;
    }

}
