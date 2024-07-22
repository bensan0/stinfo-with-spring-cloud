package com.personal.project.authservice.service;

import java.time.Duration;

public interface CacheService {

	void setCache(String key, String value, Duration duration);

	Object getCache(String key);
}
