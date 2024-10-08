package com.personal.project.authservice.service.impl;

import cn.hutool.json.JSONUtil;
import com.personal.project.authservice.model.dto.UserCacheDTO;
import com.personal.project.authservice.service.AuthService;
import com.personal.project.authservice.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

	private final CacheService cacheService;

	public AuthServiceImpl(CacheService cacheService){
		this.cacheService = cacheService;
	}

	@Override
	public UserCacheDTO authToken(String token) {

		Object cache = cacheService.getCache(token);

		if(cache == null){
			return null;
		}

		return JSONUtil.toBean(cache.toString(), UserCacheDTO.class);
	}
}
