package com.example.clothesshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    @GetMapping("/account")
    public String account(Model model) {
        return "account";
    }

    @GetMapping("/wishlist")
    public String wishlist(Model model) {
        return "wishlist";
    }
}
