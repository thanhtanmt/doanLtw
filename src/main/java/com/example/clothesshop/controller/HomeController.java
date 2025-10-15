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
}
