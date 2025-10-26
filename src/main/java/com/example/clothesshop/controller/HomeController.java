package com.example.clothesshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	@GetMapping("/")
	public String home() {
	    return "redirect:/home";
	}

	@GetMapping("/home")
	public String index() {
	    return "home";
	}

	@GetMapping("/about-us")
	public String aboutUs() {
		return "about-us";
	}

	@GetMapping("/contact")
	public String contact() {
		return "contact";
	}

	@GetMapping("/policy")
	public String policy() {
		return "policy";
	}
}
