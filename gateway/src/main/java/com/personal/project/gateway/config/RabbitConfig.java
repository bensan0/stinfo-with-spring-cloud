package com.personal.project.gateway.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitConfig {

	@Bean
	public Queue commentNotificationQueue() {
		return new Queue("Connection Info Queue");
	}

	@Bean
	public Jackson2JsonMessageConverter producerJackson2MessageConverter() {

		return new Jackson2JsonMessageConverter();
	}
}
