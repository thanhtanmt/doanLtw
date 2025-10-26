package com.example.clothesshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CartController {

    @GetMapping("/cart")
    public String cart(Model model) {
        // Thêm logic để lấy dữ liệu giỏ hàng và truyền vào model
        return "cart";
    }

    @GetMapping("/checkout")
    public String checkout(Model model) {
        // Thêm logic để chuẩn bị dữ liệu cho trang checkout
        return "checkout";
    }
}

