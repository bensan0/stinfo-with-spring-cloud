package com.personal.project.scraperservice.config;

import feign.Request;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

public class PythonFeignConfig {
	@Bean
	public Request.Options options() {
		return new Request.Options(3, TimeUnit.SECONDS, 5, TimeUnit.MINUTES, true);
	}
}

