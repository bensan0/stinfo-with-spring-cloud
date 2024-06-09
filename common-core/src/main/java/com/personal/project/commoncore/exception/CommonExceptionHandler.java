package com.personal.project.commoncore.exception;

import com.personal.project.commoncore.constants.ResponseCode;
import com.personal.project.commoncore.response.CommonResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Set;

@RestControllerAdvice
@Slf4j
public class CommonExceptionHandler {

    /**
     * 用于捕获@RequestBody类型参数触发校验规则抛出的异常
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseStatus(value = HttpStatus.OK)
    public CommonResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        StringBuilder sb = new StringBuilder();
        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
        if (!CollectionUtils.isEmpty(allErrors)) {
            for (ObjectError error : allErrors) {
                sb.append(error.getDefaultMessage()).append(";");
            }
        }

        return CommonResponse.error(ResponseCode.Invalid_Args, sb.toString(), null);
    }

    /**
     * 用于捕获@RequestParam/@PathVariable参数触发校验规则抛出的异常
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    public CommonResponse handleConstraintViolationException(ConstraintViolationException e) {
        StringBuilder sb = new StringBuilder();
        Set<ConstraintViolation<?>> conSet = e.getConstraintViolations();
        for (ConstraintViolation<?> con : conSet) {
            String message = con.getMessage();
            sb.append(message).append(";");
        }

        return CommonResponse.error(ResponseCode.Invalid_Args, sb.toString(), null);
    }

    @ExceptionHandler(value = NullPointerException.class)
    public CommonResponse handleNPE(NullPointerException e) {
        log.error("something is null", e);
        return CommonResponse.error(ResponseCode.Failed, null);
    }

    @ExceptionHandler(value = CustomException.class)
    public CommonResponse handleCustomException(CustomException e) {
        log.error("Exception: {} happened, message: {}, data, {}", e.getClass().getSimpleName(), e.getDefaultMessage(), e.getExtraData());
        return CommonResponse.error(ResponseCode.Failed, e.getDefaultMessage(), null);
    }
}
