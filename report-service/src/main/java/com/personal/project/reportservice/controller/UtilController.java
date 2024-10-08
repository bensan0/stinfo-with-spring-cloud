package com.personal.project.reportservice.controller;

import com.personal.project.commoncore.response.CommonResponse;
import com.personal.project.reportservice.constant.DetailTagEnum;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/util")
public class UtilController {

	@GetMapping("/tags")
	public CommonResponse<List<String>> getTags() {

		return CommonResponse.ok(Arrays.stream(DetailTagEnum.values())
				.map(DetailTagEnum::getTag)
				.toList());
	}
}
