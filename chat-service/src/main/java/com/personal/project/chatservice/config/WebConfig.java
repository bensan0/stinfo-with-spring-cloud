package com.personal.project.chatservice.config;

import cn.hutool.core.util.StrUtil;
import com.personal.project.chatservice.service.TGClient;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@AllArgsConstructor
@AutoConfigureAfter(TokenConfig.class)
public class WebConfig {

	private final TokenConfig tokenConfig;

	@Bean
	public TGClient tgClient() {
		HttpServiceProxyFactory httpServiceProxyFactory =
				HttpServiceProxyFactory.builderFor(
								WebClientAdapter.create(
										WebClient.builder()
												.baseUrl(StrUtil.format("https://api.telegram.org/bot{}", tokenConfig.getTg()))
												.build()
								)
						)
						.build();

		return httpServiceProxyFactory.createClient(TGClient.class);
	}
}
