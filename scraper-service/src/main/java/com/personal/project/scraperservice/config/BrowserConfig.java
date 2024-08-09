package com.personal.project.scraperservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties("scraper.resource")
public class BrowserConfig {

	private String driver;

	private String driverpath;

	private String binarypath;

}
