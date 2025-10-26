package com.example.clothesshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class OrderController {

    @GetMapping("/checkout")
    public String checkout() {
        return "checkout";
    }

    @PostMapping("/place-order")
    public String placeOrder() {
        // Chỉ chuyển hướng đến trang thành công để thiết kế
        return "redirect:/order-success";
    }

    @GetMapping("/orders")
    public String listOrders() {
        return "orders";
    }

    @GetMapping("/order/{id}")
    public String orderDetails(@PathVariable Long id, Model model) {
        // Mapping để vào trang chi tiết đơn hàng
        return "order-detail";
    }

    @GetMapping("/order-success")
    public String orderSuccess() {
        return "order-success";
    }
}
