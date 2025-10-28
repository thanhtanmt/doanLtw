package com.example.clothesshop.controller;

import com.example.clothesshop.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("")
    public String adminRoot() {
        return "redirect:/admin/dashboard";
    }

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String username = authentication.getName();
            userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .ifPresent(currentUser -> model.addAttribute("currentUser", currentUser));
        }
        return "admin/admin-profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("currentUser") com.example.clothesshop.model.User formUser,
                                RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn chưa đăng nhập");
            return "redirect:/login";
        }
        String username = authentication.getName();
        com.example.clothesshop.model.User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng");
            return "redirect:/admin/profile";
        }

        user.setFirstName(formUser.getFirstName());
        user.setLastName(formUser.getLastName());
        user.setPhone(formUser.getPhone());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Cập nhật hồ sơ thành công");
        return "redirect:/admin/profile";
    }

    @PostMapping("/profile/avatar")
    public String uploadAvatar(@ModelAttribute("file") MultipartFile file,
                               RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn chưa đăng nhập");
            return "redirect:/login";
        }
        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ảnh hợp lệ");
            return "redirect:/admin/profile";
        }
        try {
            String uploadsDir = System.getProperty("user.dir") + "/uploads";
            Files.createDirectories(Paths.get(uploadsDir));
            String filename = java.util.UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path target = Paths.get(uploadsDir, filename);
            Files.copy(file.getInputStream(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            String publicUrl = "/uploads/" + filename;

            String username = authentication.getName();
            com.example.clothesshop.model.User user = userRepository.findByUsername(username)
                    .or(() -> userRepository.findByEmail(username))
                    .orElse(null);
            if (user != null) {
                user.setAvatarUrl(publicUrl);
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
            }
            redirectAttributes.addFlashAttribute("success", "Cập nhật ảnh đại diện thành công");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải ảnh: " + e.getMessage());
        }
        return "redirect:/admin/profile";
    }
}
