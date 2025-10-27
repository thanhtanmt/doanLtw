package com.example.clothesshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/seller")
public class SellerController {

    @GetMapping("/dashboard")
    public String sellerDashboard(Model model) {
        return "seller/dashboard";
    }

    @GetMapping("/products")
    public String sellerProducts(Model model) {
        return "seller/products";
    }

    @GetMapping("/orders")
    public String sellerOrders(Model model) {
        return "seller/orders";
    }

    @GetMapping("/profile")
    public String sellerProfile(Model model) {
        return "seller/profile";
    }
}
