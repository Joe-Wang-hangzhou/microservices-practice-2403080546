package com.zjgsu.wch.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

	@Value("${spring.application.name}")
	private String appName;

	@GetMapping("/hello")
	public HelloResponse hello() {
		return new HelloResponse("图书借阅系统", appName, "Hello, library borrowing system!");
	}

	public record HelloResponse(String project, String application, String message) {
	}

}
