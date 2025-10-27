package com.example.clothesshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.clothesshop.dto.UserRegistrationDto;
import com.example.clothesshop.service.UserService;
import com.example.clothesshop.service.EmailService;
import com.example.clothesshop.model.Seller;
import com.example.clothesshop.model.User;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Random;

@Controller
@SessionAttributes("pendingUser")
public class AuthController {
	private final UserService userService;
	private final EmailService emailService;

	@Autowired
	public AuthController(UserService userService, EmailService emailService) {
		this.userService = userService;
		this.emailService = emailService;
	}

	@ModelAttribute("pendingUser")
	public UserRegistrationDto pendingUser() {
		return new UserRegistrationDto();
	}

	@GetMapping("/login")
	public String loginPage() {
		return "login";
	}

	@GetMapping("/register")
	public String showRegisterForm(Model model) {
		model.addAttribute("userDto", new UserRegistrationDto());
		return "register";
	}

	@PostMapping("/register")
	public String registerUser(@Valid @ModelAttribute("userDto") UserRegistrationDto userDto, BindingResult result,
			Model model, RedirectAttributes redirectAttributes) {
		System.out.println("==> registerUser() được gọi với email: " + userDto.getEmail());
		System.out.println("🚀 Bắt đầu xử lý đăng ký...");

		if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
			System.out.println("⚠️ Mật khẩu xác nhận không khớp");
			result.rejectValue("confirmPassword", "error.user", "Mật khẩu xác nhận không khớp");
		}

		if (userService.findByEmail(userDto.getEmail()) != null) {
			System.out.println("⚠️ Email đã tồn tại: " + userDto.getEmail());
			result.rejectValue("email", "error.user", "Email đã được sử dụng");
		}

		if (result.hasErrors()) {
			System.out.println("⚠️ Có lỗi validate form -> quay lại trang register");
			return "register";
		}

		try {
			System.out.println("✅ Bắt đầu xử lý đăng ký mới...");
			String verificationCode = generateVerificationCode();
			System.out.println("📨 Mã xác thực được tạo: " + verificationCode);

			// Lưu thông tin đăng ký vào session (sử dụng @SessionAttributes)
			userDto.setVerificationCode(verificationCode);
			userDto.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(15));
			model.addAttribute("pendingUser", userDto);

			System.out.println("📬 Chuẩn bị gửi email xác thực...");
			emailService.sendVerificationEmail(userDto.getEmail(), verificationCode);
			System.out.println("✅ Email xác thực đã gửi thành công!");

			return "redirect:/verify-email?email=" + userDto.getEmail();
		} catch (MessagingException e) {
			System.out.println("❌ Lỗi gửi email: " + e.getMessage());
			result.reject("error.email", "Không thể gửi email xác thực. Vui lòng thử lại.");
			return "register";
		} catch (Exception e) {
			System.out.println("💥 Lỗi bất ngờ khi đăng ký: " + e.getMessage());
			e.printStackTrace();
			result.reject("error.user", "Đã xảy ra lỗi. Vui lòng thử lại sau.");
			return "register";
		}
	}

	@GetMapping("/verify-email")
	public String showVerificationForm(@RequestParam(required = false) String email, Model model) {
		if (email == null || email.isEmpty()) {
			return "redirect:/login";
		}
		model.addAttribute("email", email);
		return "verify-email";
	}

	@PostMapping("/verify-email")
	public String verifyEmail(@RequestParam String email, @RequestParam String code,
			@ModelAttribute("pendingUser") UserRegistrationDto pendingUser, Model model, SessionStatus sessionStatus) {
		// Kiểm tra xem email đã tồn tại trong database chưa
		if (userService.findByEmail(email) != null) {
			model.addAttribute("error", "Email đã được đăng ký");
			return "verify-email";
		}

		// Kiểm tra thông tin xác thực trong session
		if (!email.equals(pendingUser.getEmail())) {
			model.addAttribute("error", "Email không khớp với thông tin đăng ký");
			return "verify-email";
		}

		if (pendingUser.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
			model.addAttribute("error", "Mã xác thực đã hết hạn");
			return "verify-email";
		}

		if (!pendingUser.getVerificationCode().equals(code)) {
			model.addAttribute("error", "Mã xác thực không chính xác");
			return "verify-email";
		}

		// Tạo và lưu user mới vào database
		try {
			User user = userService.registerNewUser(pendingUser);
			user.setEmailVerified(true);
			user.setEnabled(true);
			userService.save(user);

			// Xóa thông tin tạm trong session
			sessionStatus.setComplete();

			return "redirect:/login?verified=true";
		} catch (Exception e) {
			model.addAttribute("error", "Có lỗi xảy ra khi tạo tài khoản. Vui lòng thử lại.");
			return "verify-email";
		}
	}

	@PostMapping("/resend-verification")
	public String resendVerification(@RequestParam String email,
			@ModelAttribute("pendingUser") UserRegistrationDto pendingUser, Model model) {
		// Nếu user đã tồn tại trong DB
		User user = userService.findByEmail(email);

		if (user != null) {
			if (user.isEmailVerified()) {
				model.addAttribute("error", "Email đã được xác thực");
				return "verify-email";
			}

			String verificationCode = generateVerificationCode();
			user.setVerificationCode(verificationCode);
			user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(15));
			userService.save(user);

			try {
				System.out.println("📬 Chuẩn bị gửi email xác thực...");
				emailService.sendVerificationEmail(user.getEmail(), verificationCode);
				System.out.println("✅ Email xác thực đã gửi thành công!");
				model.addAttribute("message", "Mã xác thực mới đã được gửi đến email của bạn");
			} catch (MessagingException e) {
				model.addAttribute("error", "Không thể gửi email xác thực. Vui lòng thử lại.");
			}

			return "verify-email";
		}

		// Nếu user chưa được lưu (thông tin đang ở session pendingUser)
		if (pendingUser != null && email.equals(pendingUser.getEmail())) {
			String verificationCode = generateVerificationCode();
			pendingUser.setVerificationCode(verificationCode);
			pendingUser.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(15));

			try {
				System.out.println("📬 Chuẩn bị gửi email xác thực (pending)...");
				emailService.sendVerificationEmail(pendingUser.getEmail(), verificationCode);
				System.out.println("✅ Email xác thực đã gửi thành công!");
				model.addAttribute("message", "Mã xác thực mới đã được gửi đến email của bạn");
			} catch (MessagingException e) {
				model.addAttribute("error", "Không thể gửi email xác thực. Vui lòng thử lại.");
			}

			return "verify-email";
		}

		model.addAttribute("error", "Email không tồn tại");
		return "verify-email";
	}

	private String generateVerificationCode() {
		Random random = new Random();
		int code = 100000 + random.nextInt(900000); // generates 6-digit code
		return String.valueOf(code);
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
