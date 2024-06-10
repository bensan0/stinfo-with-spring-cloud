package com.personal.project.scraperservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {"com.personal.project.scraperservice.remote"})
@MapperScan("com.personal.project.scraperservice.mapper")
public class ScraperServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScraperServiceApplication.class, args);
    }

}
