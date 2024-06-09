package com.personal.project.commoncore.response;

import com.personal.project.commoncore.constants.ResponseCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PageResponse<T> extends CommonResponse<T> {

    private Long currentPage;

    private Long totalPage;

    public PageResponse(String status, String msg, T data) {
        super(status, msg, data);
    }

    public PageResponse(Long currentPage, Long totalPage, String status, String msg, T data) {
        super(status, msg, data);
        this.currentPage = currentPage;
        this.totalPage = totalPage;
    }

    public static <T> PageResponse<T> ok(Long currentPage, Long totalPage, T data) {

        return new PageResponse<>(currentPage, totalPage, ResponseCode.Success.getCode(), ResponseCode.Success.getMsg(), data);
    }

    public static <T> PageResponse<T> error(ResponseCode responseCode, String msg) {

        return new PageResponse<>(responseCode.getCode(), msg, null);
    }
}
