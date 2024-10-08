package com.personal.project.chatservice.exception.handler;

import cn.hutool.core.util.IdUtil;
import com.personal.project.commoncore.constants.ResponseCode;
import com.personal.project.commoncore.response.CommonResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestControllerAdvice
@Slf4j
public class CommonExceptionHandler {

	@ExceptionHandler(value = Exception.class)
	@ResponseStatus(value = HttpStatus.OK)
	public CommonResponse<ObjectUtils.Null> handleDefaultException(Exception e) {
		String traceCode = IdUtil.getSnowflake(1, 1).nextIdStr();

		log.error("{}, Handle default exception, trace: {}", LocalDateTime.now(), traceCode, e);

		return CommonResponse.error(ResponseCode.Failed, traceCode, null);
	}

	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	@ResponseStatus(value = HttpStatus.OK)
	public CommonResponse<ObjectUtils.Null> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		StringBuilder sb = new StringBuilder();
		List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
		if (!CollectionUtils.isEmpty(allErrors)) {
			for (ObjectError error : allErrors) {
				sb.append(error.getDefaultMessage()).append(";");
			}
		}

		return CommonResponse.error(ResponseCode.Invalid_Args, sb.toString(), null);
	}

	@ExceptionHandler(value = ConstraintViolationException.class)
	@ResponseStatus(value = HttpStatus.OK)
	public CommonResponse<ObjectUtils.Null> handleConstraintViolationException(ConstraintViolationException e) {
		StringBuilder sb = new StringBuilder();
		Set<ConstraintViolation<?>> conSet = e.getConstraintViolations();
		for (ConstraintViolation<?> con : conSet) {
			String message = con.getMessage();
			sb.append(message).append(";");
		}

		return CommonResponse.error(ResponseCode.Invalid_Args, sb.toString(), null);
	}

	@ExceptionHandler(value = NullPointerException.class)
	public CommonResponse<ObjectUtils.Null> handleNPE(NullPointerException e) {
		String traceCode = IdUtil.getSnowflake(1, 1).nextIdStr();

		log.error("something is null, trace: {}", traceCode, e);

		return CommonResponse.error(ResponseCode.Failed, "NPE, trace:" + traceCode, null);
	}
}
