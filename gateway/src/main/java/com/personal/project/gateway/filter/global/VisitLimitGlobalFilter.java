package com.personal.project.gateway.filter.global;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.personal.project.commoncore.constants.ResponseCode;
import com.personal.project.commoncore.response.CommonResponse;
import com.personal.project.gateway.config.SkipFilterConfig;
import com.personal.project.gateway.service.CacheService;
import com.personal.project.gateway.utils.IPUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class VisitLimitGlobalFilter implements GlobalFilter, Ordered {

	private final CacheService cacheService;

	private final SkipFilterConfig skipFilterConfig;

	private static final String VISIT_LIMIT_CACHE_KEY = "visit-limit:";

	private static final String BAN_FLAG_CACHE_KEY = "banned:";

	private static final Integer VISIT_LIMIT = 10;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//		ServerHttpRequest request = exchange.getRequest();
//		if (skipFilterConfig.getVisit().contains(request.getURI().getPath())) {
//
//			return chain.filter(exchange);
//		}
//
//		String ip = IPUtil.getIp(exchange.getRequest());
//
//		Object banFlag = cacheService.getCache(BAN_FLAG_CACHE_KEY + ip);
//		if (banFlag != null) {
//			try {
//				return visitedFrequently(exchange.getResponse());
//			} catch (JsonProcessingException e) {
//				throw new RuntimeException(e);
//			}
//		}
//
//		Object cache = cacheService.getCache(VISIT_LIMIT_CACHE_KEY + ip);
//
//		if (cache == null) {
//			cacheService.setCache(VISIT_LIMIT_CACHE_KEY + ip, "1", Duration.of(15, ChronoUnit.MINUTES));
//			return chain.filter(exchange);
//		}
//
//		int nowVisitTimes = Integer.parseInt(cache.toString());
//		nowVisitTimes += 1;
//		boolean expiredOrNotExisted = cacheService.setCacheWithChangingTTL(VISIT_LIMIT_CACHE_KEY + ip, String.valueOf(nowVisitTimes));
//
//		if (nowVisitTimes > VISIT_LIMIT) {
//			//ban ip
//			cacheService.setCache(BAN_FLAG_CACHE_KEY + ip, "1", Duration.of(30, ChronoUnit.MINUTES));
//
//			try {
//				return visitedFrequently(exchange.getResponse());
//			} catch (JsonProcessingException e) {
//				throw new RuntimeException(e);
//			}
//		}

		return chain.filter(exchange);
	}

	@Override
	public int getOrder() {
		return -1;
	}

	private Mono<Void> visitedFrequently(ServerHttpResponse response) throws JsonProcessingException {
		response.setStatusCode(HttpStatus.OK);
		CommonResponse<Object> error = CommonResponse.error(ResponseCode.Not_Valid.getCode(), "你是不是機器人？蛤？", null);
		byte[] bytes = JSONUtil.toJsonStr(error).getBytes(StandardCharsets.UTF_8);
		DataBuffer buffer = response.bufferFactory().wrap(bytes);

		return response.writeWith(Flux.just(buffer));
	}
}
