package com.example.clothesshop.controller;

import org.springframework.stereotype.Controller;  // ⬅️ Import thêm dòng này
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.clothesshop.model.Seller;

@Controller  // ✅ Bắt buộc phải có
public class ShipperController {

    @GetMapping("/shipper/shipped")
    public String shipped(Model model) {
        model.addAttribute("seller", new Seller());
        return "/shipper/pending";
    }

    @GetMapping("/shipper/shipping")
    public String shipping(Model model) {
        model.addAttribute("seller", new Seller());
        return "/shipper/completed";
    }
    @GetMapping("/shipper/profile")
    public String shipperInfor(Model model) {
        model.addAttribute("seller", new Seller());
        return "/shipper/profile";
    }
}
