package com.personal.project.gateway.filter.global;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.json.JSONUtil;
import com.personal.project.commoncore.constants.ResponseCode;
import com.personal.project.commoncore.response.CommonResponse;
import com.personal.project.gateway.config.SkipFilterConfig;
import com.personal.project.gateway.model.dto.ConnectionInfoDTO;
import com.personal.project.gateway.model.dto.TokenDTO;
import com.personal.project.gateway.model.dto.UserCacheDTO;
import com.personal.project.gateway.msgqueue.ConnectionProducer;
import com.personal.project.gateway.remote.RemoteAuthService;
import com.personal.project.gateway.utils.IPUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

	private final RemoteAuthService remoteAuthService;

	private final SkipFilterConfig skipFilterConfig;

	private final ConnectionProducer producer;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();

		logConnectionInfo(request);

		//跳過不需驗證的路由
		if (skipFilterConfig.getAuth().contains(request.getURI().getPath())) {
			return chain.filter(exchange);
		}

		List<String> headerToken = request.getHeaders().get("AuthToken");
		String token = headerToken == null ? "" : headerToken.getFirst();

		if (token == null || token.isEmpty()) {
			return getVoidMono(exchange.getResponse(), ResponseCode.Not_Valid, "token should not be null or empty");
		}

		return Mono.just(token)
				.map(t -> {
					TokenDTO dto = new TokenDTO();
					dto.setToken(t);
					return dto;
				})
				.flatMap(remoteAuthService::authToken)
				.flatMap(response -> {
					if (response == null) {
						ServerHttpResponse exResponse = exchange.getResponse();

						return getVoidMono(exResponse, ResponseCode.Failed, "auth service error");
					}

					if (response.getData() == null || response.getData().toString().equals("null")) {
						//有token 無cache
						ServerHttpResponse exResponse = exchange.getResponse();

						return getVoidMono(exResponse, ResponseCode.Token_Expired, "token is expired");
					}

					UserCacheDTO userCache = BeanUtil.copyProperties(response.getData(), UserCacheDTO.class);

//					ServerHttpResponse httpResponse = exchange.getResponse();
//					httpResponse.getHeaders().add("userId", userCache.getId().toString());
//					httpResponse.getHeaders().add("username", userCache.getUsername());

					ServerHttpRequest mutate = request.mutate()
							.headers(httpHeaders -> httpHeaders.add("userId", userCache.getId().toString()))
							.headers(httpHeaders -> httpHeaders.add("username", userCache.getUsername()))
							.build();

					return chain.filter(exchange.mutate().request(mutate).build());
				})
				.onErrorResume(e -> {
					log.error("Error during authentication", e);
					return getVoidMono(exchange.getResponse(), ResponseCode.Failed, "server something go wrong");
				});
	}

	@Override
	public int getOrder() {
		return -2;
	}

	private Mono<Void> getVoidMono(ServerHttpResponse serverHttpResponse, ResponseCode responseCode, String msg) {
		serverHttpResponse.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
		CommonResponse<ObjectUtils.Null> responseResult = CommonResponse.error(responseCode.getCode(), msg.isEmpty() ? responseCode.getMsg() : msg, null);
		DataBuffer dataBuffer = serverHttpResponse.bufferFactory().wrap(JSONUtil.toJsonStr(responseResult).getBytes());
		return serverHttpResponse.writeWith(Flux.just(dataBuffer));
	}

	private void logConnectionInfo(ServerHttpRequest request) {
		URI uri = request.getURI();
		String ip = IPUtil.getIp(request);
		String date = LocalDateTime.now().format(DatePattern.PURE_DATETIME_FORMATTER);
		producer.send(new ConnectionInfoDTO(ip, uri.getPath(), date));
	}
}
