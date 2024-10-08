package com.personal.project.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties("custom.gateway.skip")
public class SkipFilterConfig {

	private List<String> visit;

	private List<String> auth;
}
