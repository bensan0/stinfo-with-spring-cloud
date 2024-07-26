package com.personal.project.gateway.remote;

import cn.hutool.json.JSONUtil;
import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.gateway.model.dto.TokenDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class RemoteAuthServiceImpl implements RemoteAuthService {

	private final static String LB = "http://auth-service";

	private final WebClient webClient;

	public RemoteAuthServiceImpl(WebClient webClient) {
		this.webClient = webClient;
	}

	@Override
	public Mono<InnerResponse> authToken(TokenDTO dto) {

		return webClient
				.post()
				.uri(LB + "/feign/auth/auth-token")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(JSONUtil.toJsonStr(dto))
				.retrieve()
				.bodyToMono(String.class)
				.map(body -> JSONUtil.toBean(body, InnerResponse.class))
				.onErrorResume(e -> {
					log.error("Call remote auth token wrong", e);
					return Mono.empty();
				});
	}
}
