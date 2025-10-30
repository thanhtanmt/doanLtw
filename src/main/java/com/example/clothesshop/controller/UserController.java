package com.example.clothesshop.controller;

import com.example.clothesshop.model.User;
import com.example.clothesshop.repository.UserRepository;
import com.example.clothesshop.service.CloudinaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class UserController {

    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository,
                         CloudinaryService cloudinaryService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/account")
    public String account(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .or(() -> userRepository.findByEmail(username))
                    .orElse(null);
            model.addAttribute("user", user);
        }
        return "account";
    }

    // ===== API ENDPOINTS FOR PROFILE MANAGEMENT =====
    
    /**
     * Update user profile (firstName, lastName, phone, address)
     */
    @PostMapping("/api/user/profile")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestParam(required = false) String firstName,
                                                             @RequestParam(required = false) String lastName,
                                                             @RequestParam(required = false) String phone,
                                                             @RequestParam(required = false) String address,
                                                             @RequestParam(required = false) MultipartFile avatar) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                response.put("success", false);
                response.put("message", "Bạn chưa đăng nhập");
                return ResponseEntity.status(401).body(response);
            }
            
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .or(() -> userRepository.findByEmail(username))
                    .orElse(null);
                    
            if (user == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy người dùng");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Update profile fields
            if (firstName != null && !firstName.trim().isEmpty()) {
                user.setFirstName(firstName.trim());
            }
            if (lastName != null && !lastName.trim().isEmpty()) {
                user.setLastName(lastName.trim());
            }
            if (phone != null && !phone.trim().isEmpty()) {
                user.setPhone(phone.trim());
            }
            if (address != null) {
                user.setAddress(address.trim());
            }
            
            // Handle avatar upload if provided
            if (avatar != null && !avatar.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(avatar, "user-avatars");
                
                // Delete old avatar if exists
                if (user.getAvatarUrl() != null && user.getAvatarUrl().contains("cloudinary.com")) {
                    try {
                        String oldPublicId = cloudinaryService.extractPublicId(user.getAvatarUrl());
                        cloudinaryService.deleteImage(oldPublicId);
                    } catch (Exception e) {
                        System.err.println("Không thể xóa ảnh cũ: " + e.getMessage());
                    }
                }
                
                user.setAvatarUrl(imageUrl);
            }
            
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            response.put("success", true);
            response.put("message", "Cập nhật thông tin thành công");
            response.put("data", createUserData(user));
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Lỗi khi upload ảnh: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Upload/Update user avatar only
     */
    @PostMapping("/api/user/avatar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                response.put("success", false);
                response.put("message", "Bạn chưa đăng nhập");
                return ResponseEntity.status(401).body(response);
            }
            
            if (file == null || file.isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng chọn ảnh hợp lệ");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "File phải là ảnh (jpg, png, gif, etc.)");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "Kích thước ảnh không được vượt quá 5MB");
                return ResponseEntity.badRequest().body(response);
            }
            
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .or(() -> userRepository.findByEmail(username))
                    .orElse(null);
                    
            if (user == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy người dùng");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Upload to Cloudinary
            String imageUrl = cloudinaryService.uploadImage(file, "user-avatars");
            
            // Delete old avatar if exists
            if (user.getAvatarUrl() != null && user.getAvatarUrl().contains("cloudinary.com")) {
                try {
                    String oldPublicId = cloudinaryService.extractPublicId(user.getAvatarUrl());
                    cloudinaryService.deleteImage(oldPublicId);
                } catch (Exception e) {
                    System.err.println("Không thể xóa ảnh cũ: " + e.getMessage());
                }
            }
            
            user.setAvatarUrl(imageUrl);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            response.put("success", true);
            response.put("message", "Cập nhật ảnh đại diện thành công");
            response.put("avatarUrl", imageUrl);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Lỗi khi upload ảnh: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Change user password
     */
    @PostMapping("/api/change-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                response.put("success", false);
                response.put("message", "Bạn chưa đăng nhập");
                return ResponseEntity.status(401).body(response);
            }
            
            String currentPassword = payload.get("currentPassword");
            String newPassword = payload.get("newPassword");
            
            if (currentPassword == null || currentPassword.isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng nhập mật khẩu hiện tại");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (newPassword == null || newPassword.isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng nhập mật khẩu mới");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (newPassword.length() < 6) {
                response.put("success", false);
                response.put("message", "Mật khẩu mới phải có ít nhất 6 ký tự");
                return ResponseEntity.badRequest().body(response);
            }
            
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .or(() -> userRepository.findByEmail(username))
                    .orElse(null);
                    
            if (user == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy người dùng");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                response.put("success", false);
                response.put("message", "Mật khẩu hiện tại không đúng");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            response.put("success", true);
            response.put("message", "Đổi mật khẩu thành công");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Helper method to create user data map
     */
    private Map<String, Object> createUserData(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
        data.put("firstName", user.getFirstName());
        data.put("lastName", user.getLastName());
        data.put("phone", user.getPhone());
        data.put("address", user.getAddress());
        data.put("avatarUrl", user.getAvatarUrl());
        return data;
    }

    // Wishlist mapping đã được chuyển sang WishlistController
}
