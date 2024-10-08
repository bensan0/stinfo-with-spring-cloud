package com.personal.project.authservice.controller;

import com.personal.project.authservice.model.dto.ChangePasswordRequestDTO;
import com.personal.project.authservice.model.dto.CheckTokenRequestDTO;
import com.personal.project.authservice.model.dto.UserSignUpRequestDTO;
import com.personal.project.authservice.service.UserService;
import com.personal.project.commoncore.constants.ResponseCode;
import com.personal.project.commoncore.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sign")
@Slf4j
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

	@PostMapping("/check-token")
	public CommonResponse<Boolean> checkToken(
			@Valid @RequestBody CheckTokenRequestDTO dto
	) {
		Boolean isValid = userService.checkToken(dto.getToken());

		return CommonResponse.ok(isValid);
	}

	@PostMapping("/change-password")
	public CommonResponse<ObjectUtils.Null> changePassword(
			@Valid @RequestBody ChangePasswordRequestDTO dto
	) {
		Boolean changed = userService.changePassword(dto);
		if (changed) {
			return CommonResponse.ok(null);
		} else {
			return CommonResponse.error(ResponseCode.Not_Valid.getCode(), "Now password wrong", null);
		}
	}
}
