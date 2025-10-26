package com.example.clothesshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.clothesshop.dto.UserRegistrationDto;
import com.example.clothesshop.service.UserService;

import jakarta.validation.Valid;

@Controller
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto userDto,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        // Kiểm tra password matching
        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.user", "Mật khẩu xác nhận không khớp");
        }

        // Nếu có lỗi validation, trả về form
        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.registerNewUser(userDto);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký tài khoản thành công!");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            result.rejectValue("username", "error.user", e.getMessage());
            return "register";
        }
    }
}
