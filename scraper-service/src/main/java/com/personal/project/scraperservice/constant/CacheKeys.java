package com.personal.project.scraperservice.constant;

import lombok.Getter;

@Getter
public enum CacheKeys {

	INIT_DATA_CACHE("data:init", "scrape-init-lock"),
	SCRAPE_INFO_CACHE("info:scrape", "scrape-info-lock"),
	GEN_METRICS_CACHE("metrics:gen", "gen-metrics-lock"),
	GEN_DETAIL_CACHE("detail:gen", "gen-detail-lock");

	final String key;

	final String lock;

	CacheKeys(String key, String lock) {
		this.key = key;
		this.lock = lock;
	}
}
