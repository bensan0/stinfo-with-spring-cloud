package com.personal.project.authservice.controller;

import com.personal.project.authservice.model.dto.TokenDTO;
import com.personal.project.authservice.model.dto.UserCacheDTO;
import com.personal.project.authservice.service.AuthService;
import com.personal.project.commoncore.response.InnerResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feign/auth")
public class FeignController {

	private final AuthService authService;

	public FeignController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/auth-token")
	public InnerResponse<UserCacheDTO> authToken(
			@RequestBody TokenDTO dto
	) {

		UserCacheDTO userCacheDTO = authService.authToken(dto.getToken());

		return InnerResponse.ok(userCacheDTO);
	}
}
