package com.personal.project.gateway.service;

import java.time.Duration;

public interface CacheService {

	void setCache(String key, String value, Duration duration);

	boolean setCacheWithChangingTTL(String key, String value);

	void setCache(String key, String value);

	Object getCache(String key);
}
