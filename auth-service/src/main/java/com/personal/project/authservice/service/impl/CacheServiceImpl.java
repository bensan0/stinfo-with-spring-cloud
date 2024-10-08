package com.personal.project.authservice.service.impl;

import com.personal.project.authservice.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
public class CacheServiceImpl implements CacheService {

	private final RedissonClient redissonClient;

	public CacheServiceImpl(RedissonClient redissonClient){
		this.redissonClient = redissonClient;
	}

	@Override
	public void setCache(String key, String value, Duration duration){
		RBucket<Object> bucket = redissonClient.getBucket(key);
		bucket.set(value, duration);
	}

	@Override
	public Object getCache(String key) {
		RBucket<Object> bucket = redissonClient.getBucket(key);
		return bucket.get();
	}
}
