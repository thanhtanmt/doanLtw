package com.example.clothesshop.service.impl;

import com.example.clothesshop.model.User;
import com.example.clothesshop.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        System.out.println("========================================");
        System.out.println("🔍 Loading user by username/email: " + input);
        
        // Tìm user theo username hoặc email
        User u = userRepository.findByUsername(input)
                .or(() -> userRepository.findByEmail(input)) // nếu không có username thì thử email
                .orElseThrow(() -> {
                    System.out.println("❌ User not found: " + input);
                    System.out.println("========================================");
                    return new UsernameNotFoundException("Không tìm thấy người dùng: " + input);
                });

        System.out.println("✅ User found: " + u.getUsername());
        System.out.println("   Email: " + u.getEmail());
        System.out.println("   Enabled: " + u.isEnabled());
        System.out.println("   Email Verified: " + u.isEmailVerified());
        System.out.println("   Roles: " + u.getRoles().stream()
                .map(r -> r.getName()).collect(java.util.stream.Collectors.joining(", ")));
        System.out.println("   Password hash starts with: " + u.getPassword().substring(0, Math.min(20, u.getPassword().length())));
        System.out.println("========================================");

        return org.springframework.security.core.userdetails.User.builder()
                .username(u.getUsername()) // vẫn dùng username làm định danh chính
                .password(u.getPassword())
                .disabled(!u.isEnabled())
                .authorities(u.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(r.getName()))
                        .collect(Collectors.toSet()))
                .build();
    }

}
