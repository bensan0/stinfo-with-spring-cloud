package com.personal.project.stockservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/p")
public class PageController {

	@GetMapping("/main-page")
	public String mainPage(){
		return "mainpage";
	}

	@GetMapping("/real-time-page")
	public String rtPage(){
		return "real-time-page";
	}
}
