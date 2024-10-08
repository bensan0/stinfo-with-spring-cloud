package com.personal.project.authservice.service;

import com.personal.project.authservice.model.dto.UserCacheDTO;

public interface AuthService {

	UserCacheDTO authToken(String token);
}
