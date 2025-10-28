package com.example.clothesshop;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class để generate BCrypt password hash
 * Chạy class này để tạo password hash mới
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Các password cần hash
        String[] passwords = {
            "123456",  // Password mặc định cho test
            "admin123",     // Password cho admin
            "seller123",    // Password cho seller
            "shipper123",   // Password cho shipper
            "user123"       // Password cho user
        };
        
        System.out.println("=".repeat(80));
        System.out.println("BCrypt Password Hash Generator");
        System.out.println("=".repeat(80));
        System.out.println();
        
        for (String password : passwords) {
            String hash = encoder.encode(password);
            System.out.println("Plain password: " + password);
            System.out.println("BCrypt hash:    " + hash);
            System.out.println("-".repeat(80));
        }
        
        System.out.println();
        System.out.println("Copy các hash này vào SQL script để tạo user với password tương ứng");
        System.out.println();
        
        // Demo verify password
        String testPassword = "password123";
        String testHash = encoder.encode(testPassword);
        boolean matches = encoder.matches(testPassword, testHash);
        
        System.out.println("=".repeat(80));
        System.out.println("Password verification test:");
        System.out.println("Password: " + testPassword);
        System.out.println("Hash:     " + testHash);
        System.out.println("Matches:  " + matches);
        System.out.println("=".repeat(80));
    }
}
