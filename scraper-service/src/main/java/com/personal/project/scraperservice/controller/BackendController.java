package com.personal.project.scraperservice.controller;

import com.personal.project.commoncore.response.CommonResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scraper")
public class BackendController {


    @PostMapping("/calculate")
    public CommonResponse test() {
        return null;
    }

}
