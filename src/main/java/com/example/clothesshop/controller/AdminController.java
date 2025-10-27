package com.example.clothesshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        return "admin/dashboard";
    }

    @GetMapping("/products")
    public String adminProducts(Model model) {
        return "admin/products";
    }

    @GetMapping("/customers")
    public String adminCustomers(Model model) {
        return "admin/customers";
    }

    @GetMapping("/transactions")
    public String adminTransactions(Model model) {
        return "admin/transactions";
    }

    @GetMapping("/profile")
    public String adminProfile(Model model) {
        return "admin/admin-profile";
    }
}
