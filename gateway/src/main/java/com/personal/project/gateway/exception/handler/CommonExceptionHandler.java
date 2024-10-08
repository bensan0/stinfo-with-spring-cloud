package com.personal.project.gateway.exception.handler;

import cn.hutool.core.util.IdUtil;
import com.personal.project.commoncore.constants.ResponseCode;
import com.personal.project.commoncore.response.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.server.EntityResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class CommonExceptionHandler {

	@ExceptionHandler(Exception.class)
	public Mono<EntityResponse<CommonResponse<Object>>> handleDefaultException(Exception e) {
		String traceCode = IdUtil.getSnowflake(1, 1).nextIdStr();

		log.error("{}, Handle default exception, trace: {}", LocalDateTime.now(), traceCode, e);

		return EntityResponse.fromObject(
						CommonResponse.error(ResponseCode.Failed, "trace: " + traceCode, null)
				)
				.status(HttpStatus.OK)
				.build();
	}
}
