package com.personal.project.gateway.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

	/**
	 * webclient原生不支持負載均衡機制, 意味著無法解析lb開頭的地址, 因此要配合Spring Cloud LoadBalancer增加@LoadBalanced來實現
	 * 並且將原先的地址lb://myservice替換為http://myservice
	 * @return
	 */
	@Bean
	public WebClient loadBalancedWebClientBuilder(ReactorLoadBalancerExchangeFilterFunction lbFunc) {
		return WebClient.builder()
				.filter(lbFunc)
				.clientConnector(new ReactorClientHttpConnector(HttpClient.create().responseTimeout(Duration.ofSeconds(5))))
				.build();
	}
}