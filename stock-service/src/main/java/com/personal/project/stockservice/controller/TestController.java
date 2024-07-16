package com.personal.project.stockservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/testing")
public class TestController {

	@GetMapping("/say-hello")
	public String hello(){
		return "hello";
	}
}
