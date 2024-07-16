package com.personal.project.scraperservice.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

	private final RedisProperties redisProperties;

	public RedissonConfig(RedisProperties redisProperties) {
		this.redisProperties = redisProperties;
	}

	@Bean
	public RedissonClient redissonClient() {
		Config config = new Config();
		config.useSingleServer().setAddress("redis://" + redisProperties.getHost() + ":" + redisProperties.getPort())
				.setDatabase(redisProperties.getDatabase())
				.setTimeout(5000)
				.setConnectTimeout(5000)
				.setPassword(redisProperties.getPassword() == null ? null : redisProperties.getPassword());

		return Redisson.create(config);
	}
}
