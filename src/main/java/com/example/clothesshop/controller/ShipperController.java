package com.example.clothesshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/shipper")
public class ShipperController {

    @GetMapping("/dashboard")
    public String shipperDashboard(Model model) {
        return "shipper/dashboard";
    }

    @GetMapping("/shipped")
    public String shipped(Model model) {
        return "shipper/pending";
    }

    @GetMapping("/shipping")
    public String shipping(Model model) {
        return "shipper/completed";
    }
    
    @GetMapping("/profile")
    public String shipperInfor(Model model) {
        return "shipper/profile";
    }
 // Partial views for profile sections
    @GetMapping("/partial/profile-info")
    public String profileInfo() {
        return "shipper/partials/profile-info-partial";
    }
    
    @GetMapping("/partial/security")
    public String security() {
        return "shipper/partials/security-partial";
    }
    
    @GetMapping("/partial/policy")
    public String policy() {
        return "shipper/partials/policy-partial";
    }
}
