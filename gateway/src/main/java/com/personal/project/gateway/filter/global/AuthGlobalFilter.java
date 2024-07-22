package com.personal.project.gateway.filter.global;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.gateway.CustomConfig;
import com.personal.project.gateway.model.TokenDTO;
import com.personal.project.gateway.model.UserCacheDTO;
import com.personal.project.gateway.remote.RemoteAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.AddRequestHeaderGatewayFilterFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Lazy})//使用延遲注入以解決filter引用feign造成的循環依賴問題
public class AuthGlobalFilter implements GlobalFilter, Ordered {

	private final RemoteAuthService remoteAuthService;

	private final CustomConfig customConfig;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		//跳過不需驗證的路由
		if (customConfig.getSkip().contains(exchange.getRequest().getURI().getPath())) {

			return chain.filter(exchange);
		}

//		String token = exchange.getRequest().getHeaders().getFirst("token");
		// 獲取所有 cookie
		MultiValueMap<String, HttpCookie> cookies = exchange.getRequest().getCookies();

		// 獲取特定的 cookie，例如名為 "token" 的 cookie
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

						return redirectToLogin(exchange.getResponse());
					}

					UserCacheDTO userCache = BeanUtil.copyProperties(response.getData(), UserCacheDTO.class);

					if (userCache == null) {

						return redirectToLogin(exchange.getResponse());
					}

					ServerHttpRequest request = exchange.getRequest().mutate()
							.headers(httpHeaders -> httpHeaders.add("userId", userCache.getId().toString()))
							.build();

					return chain.filter(exchange.mutate().request(request).build());
				})
				.onErrorResume(e -> {
					log.error("Error during authentication", e);
					return redirectToLogin(exchange.getResponse());
				});
	}

	@Override
	public int getOrder() {
		return -1;
	}

	private Mono<Void> redirectToLogin(ServerHttpResponse response) {
		String redirectUrl = "http://localhost:8999/gw/auth/p/login";
		response.getHeaders().set(HttpHeaders.LOCATION, redirectUrl);
		response.setStatusCode(HttpStatus.FOUND);
		return response.setComplete();
	}
}
