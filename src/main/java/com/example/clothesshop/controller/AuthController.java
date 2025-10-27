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
import com.example.clothesshop.model.Seller;

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
//	public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto userDto, BindingResult result,
//			RedirectAttributes redirectAttributes) {
//		// Kiểm tra password matching
//		if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
//			result.rejectValue("confirmPassword", "error.user", "Mật khẩu xác nhận không khớp");
//		}
//
//		// Nếu có lỗi validation, trả về form
//		if (result.hasErrors()) {
//			return "register";
//		}
//
//		try {
//			userService.registerNewUser(userDto);
//			redirectAttributes.addFlashAttribute("successMessage", "Đăng ký tài khoản thành công!");
//			return "redirect:/login";
//		} catch (IllegalArgumentException e) {
//			result.rejectValue("username", "error.user", e.getMessage());
//			return "register";
//		}
//	}

	public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto userDto, BindingResult result,
			RedirectAttributes redirectAttributes) {
		System.out.println("==> Hàm registerUser() được gọi");

		// Kiểm tra password matching
		if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
			System.out.println("!! Mật khẩu xác nhận không khớp");
			result.rejectValue("confirmPassword", "error.user", "Mật khẩu xác nhận không khớp");
		}

		// Nếu có lỗi validation, in chi tiết từng lỗi
		if (result.hasErrors()) {
			System.out.println("==> Có lỗi validation, chi tiết:");
			result.getAllErrors().forEach(error -> {
				System.out.println(" - " + error.getObjectName() + ": " + error.getDefaultMessage());
			});
			return "register";
		}

		try {
			System.out.println("==> Gọi userService.registerNewUser()");
			userService.registerNewUser(userDto);

			System.out.println("==> Đăng ký thành công, chuyển hướng đến /login");
			redirectAttributes.addFlashAttribute("successMessage", "Đăng ký tài khoản thành công!");
			return "redirect:/login";
		} catch (IllegalArgumentException e) {
			System.out.println("!! Lỗi khi đăng ký: " + e.getMessage());
			e.printStackTrace(); // In toàn bộ stack trace để dễ debug
			result.rejectValue("username", "error.user", e.getMessage());
			return "register";
		} catch (Exception e) {
			System.out.println("!! Lỗi không xác định khi đăng ký: " + e.getMessage());
			e.printStackTrace(); // In toàn bộ lỗi chi tiết
			result.reject("error.user", "Đã xảy ra lỗi không xác định. Vui lòng thử lại sau.");
			return "register";
		}
	}

	@GetMapping("/seller/register")
	public String sellerRegister(Model model) {
	    model.addAttribute("seller", new Seller()); // ✅ thêm dòng này
	    return "seller-register";
	}
	
	@GetMapping("/shipper/dashboard")
	public String shipper(Model model) {
	    model.addAttribute("seller", new Seller()); // ✅ thêm dòng này
	    return "/shipper/dashboard";
	}
}
