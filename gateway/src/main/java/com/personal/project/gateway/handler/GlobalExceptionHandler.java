package com.personal.project.gateway.handler;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@Order(-1)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        if (ex instanceof ResponseStatusException) {
            response.setStatusCode(((ResponseStatusException) ex).getStatusCode());
        }

        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();
            try {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 500);
                result.put("error", ex.getClass().getSimpleName());
                result.put("errorMessage", ex.getMessage());

                return bufferFactory.wrap(JSONUtil.toJsonStr(result).getBytes());
            } catch (Exception e) {
                log.error("Exception when handle response error, Exception {}, response {}", e.getClass().getSimpleName(), JSONUtil.toJsonStr(response), e);
                Map<String, Object> innerError = new HashMap<>();
                innerError.put("code", 500);
                innerError.put("error", e.getClass().getSimpleName());
                innerError.put("errorMessage", "inner error: " + e.getMessage());

                return bufferFactory.wrap(JSONUtil.toJsonStr(innerError).getBytes());
            }
        }));
    }
}
