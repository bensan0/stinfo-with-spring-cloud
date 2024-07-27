package com.personal.project.gateway.service.impl;


import com.personal.project.gateway.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class CacheServiceImpl implements CacheService {

	private final RedissonClient redissonClient;

	public CacheServiceImpl(RedissonClient redissonClient) {
		this.redissonClient = redissonClient;
	}

	@Override
	public void setCache(String key, String value, Duration duration) {
		RBucket<Object> bucket = redissonClient.getBucket(key);
		bucket.set(value, duration);
	}

	@Override
	public boolean setCacheWithChangingTTL(String key, String value) {
		RBucket<Object> bucket = redissonClient.getBucket(key);
		long timeToLive = bucket.remainTimeToLive();
		if (timeToLive == -1 || timeToLive == -2) {
			return true;
		}
		bucket.set(value, Duration.of(timeToLive, ChronoUnit.MILLIS));

		return false;
	}

	@Override
	public void setCache(String key, String value) {
		RBucket<Object> bucket = redissonClient.getBucket(key);
		bucket.set(value);
	}

	@Override
	public Object getCache(String key) {
		RBucket<Object> bucket = redissonClient.getBucket(key);
		return bucket.get();
	}
}
