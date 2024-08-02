package com.personal.project.authservice.controller;

import com.personal.project.authservice.model.dto.UserSignUpRequestDTO;
import com.personal.project.authservice.service.UserService;
import com.personal.project.commoncore.response.CommonResponse;
import jakarta.validation.Valid;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sign")
public class PublicController {

	private final UserService userService;

	public PublicController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/sign-up")
	public CommonResponse<ObjectUtils.Null> signUp(
			@Valid @RequestBody UserSignUpRequestDTO dto
	) {
		userService.signUp(dto);

		return CommonResponse.ok(null);
	}

	@PostMapping("/sign-in")
	public CommonResponse<String> signIn(
			@Valid @RequestBody UserSignUpRequestDTO dto
	) {
		String token = userService.signIn(dto);

		return CommonResponse.ok(token);
	}
}
