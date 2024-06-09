package com.personal.project.commoncore.response;

import com.personal.project.commoncore.constants.ResponseCode;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
public class CommonResponse<T> {

    protected String status;

    protected String msg;

    protected T data;

    public CommonResponse(String status, String msg, T data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public static <T> CommonResponse<T> ok(T data) {

        return new CommonResponse<>(ResponseCode.Success.getCode(), ResponseCode.Success.getMsg(), data);
    }

    public static <T> CommonResponse<T> error(ResponseCode responseCode, T data) {
        return new CommonResponse<>(responseCode.getCode(), responseCode.getMsg(), data);
    }

    public static <T> CommonResponse<T> error(ResponseCode responseCode, String msg, T data) {

        return StringUtils.isEmpty(msg) ? error(responseCode, data) : new CommonResponse<>(responseCode.getCode(), msg, data);
    }

    public static <T> CommonResponse<T> error(String code, String msg, T data) {

        return new CommonResponse<>(code, msg, data);
    }
}
