package com.personal.project.gateway.filter.global;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.personal.project.gateway.config.SkipFilterConfig;
import com.personal.project.gateway.model.dto.ConnectionInfoDTO;
import com.personal.project.gateway.model.dto.TokenDTO;
import com.personal.project.gateway.model.dto.UserCacheDTO;
import com.personal.project.gateway.msgqueue.ConnectionProducer;
import com.personal.project.gateway.remote.RemoteAuthService;
import com.personal.project.gateway.utils.IPUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;

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

		// 獲取所有 cookie
		MultiValueMap<String, HttpCookie> cookies = request.getCookies();

		// 獲取特定cookie
		String token = cookies.getFirst("token") != null ? cookies.getFirst("token").getValue() : null;
		if (StrUtil.isBlank(token)) {
			ServerHttpResponse response = exchange.getResponse();

			//返回自定義錯誤訊息給前端
//			CommonResponse<ObjectUtils.Null> resData = CommonResponse.error(ResponseCode.Not_Valid.getCode(), "Without token", null);
//			ServerHttpResponse response = exchange.getResponse();
//			response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
//			DataBuffer dataBuffer = response.bufferFactory().wrap(JSONUtil.toJsonStr(resData).getBytes(StandardCharsets.UTF_8));
//			return response.writeWith(Mono.just(dataBuffer));

			//直接重定向
			return redirectToLogin(response);
		}

		return Mono.just(token)
				.map(t -> {
					TokenDTO dto = new TokenDTO();
					dto.setToken(t);
					return dto;
				})
				.flatMap(remoteAuthService::authToken)
				.flatMap(response -> {
					if (response == null || response.getData() == null || response.getData().toString().equals("null")) {
						//有cookie 無cache
						ServerHttpResponse exResponse = exchange.getResponse();
						expireCookie(exResponse);
						return redirectToLogin(exResponse);
					}

					UserCacheDTO userCache = BeanUtil.copyProperties(response.getData(), UserCacheDTO.class);

					if (userCache == null) {
						ServerHttpResponse exResponse = exchange.getResponse();
						expireCookie(exResponse);
						return redirectToLogin(exResponse);
					}

					ServerHttpRequest mutate = request.mutate()
							.headers(httpHeaders -> httpHeaders.add("userId", userCache.getId().toString()))
							.build();

					return chain.filter(exchange.mutate().request(mutate).build());
				})
				.onErrorResume(e -> {
					log.error("Error during authentication", e);
					return redirectToLogin(exchange.getResponse());
				});
	}

	@Override
	public int getOrder() {
		return -2;
	}

	private Mono<Void> redirectToLogin(ServerHttpResponse response) {
		String redirectUrl = "http://localhost:8999/gw/auth/p/login";
		response.getHeaders().set(HttpHeaders.LOCATION, redirectUrl);
		response.setStatusCode(HttpStatus.FOUND);
		return response.setComplete();
	}

	private void logConnectionInfo(ServerHttpRequest request) {
		URI uri = request.getURI();
		String ip = IPUtil.getIp(request);
		String date = LocalDateTime.now().format(DatePattern.PURE_DATETIME_FORMATTER);
		producer.send(new ConnectionInfoDTO(ip, uri.getPath(), date));
	}

	private void expireCookie(ServerHttpResponse response){
		ResponseCookie cookie = ResponseCookie.from("token", "")
				.maxAge(0)
				.path("/")
				.build();
		response.addCookie(cookie);
	}
}
