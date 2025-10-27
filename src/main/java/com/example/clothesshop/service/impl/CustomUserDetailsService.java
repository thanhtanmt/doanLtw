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
        // Tìm user theo username hoặc email
        User u = userRepository.findByUsername(input)
                .or(() -> userRepository.findByEmail(input)) // nếu không có username thì thử email
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + input));

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
