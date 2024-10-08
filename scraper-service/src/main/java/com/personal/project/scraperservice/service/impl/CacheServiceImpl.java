package com.personal.project.scraperservice.service.impl;

import com.personal.project.scraperservice.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CacheServiceImpl implements CacheService {

	private final RedissonClient redissonClient;

	public CacheServiceImpl(RedissonClient redissonClient){
		this.redissonClient = redissonClient;
	}

	@Override
	public RLock getLock(String lock) {

		return redissonClient.getLock(lock);
	}
}
