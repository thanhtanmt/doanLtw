package com.example.clothesshop.service.impl;

import com.example.clothesshop.model.User;
import com.example.clothesshop.model.Role;
import com.example.clothesshop.repository.UserRepository;
import com.example.clothesshop.repository.RoleRepository;
import com.example.clothesshop.service.UserService;
import com.example.clothesshop.dto.UserRegistrationDto;

import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.HashSet;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepo, RoleRepository roleRepo, PasswordEncoder encoder) {
        this.userRepository = userRepo;
        this.roleRepository = roleRepo;
        this.passwordEncoder = encoder;
    }

    @Override
    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

	@Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User registerNewUser(UserRegistrationDto registrationDto) {
        // Validate password match
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        // Check if username already exists
        if (existsByUsername(registrationDto.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        // Tạo một đối tượng User từ DTO nhưng KHÔNG lưu vào DB ở đây.
        // Việc lưu và mã hóa password được thực hiện khi gọi save(user).
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setPassword(registrationDto.getPassword()); // raw password, will be encoded in save()
        user.setEmail(registrationDto.getEmail());
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setPhone(registrationDto.getPhone());
        user.setEnabled(false); // sẽ bật khi user xác thực email

        // Set role mặc định là ROLE_USER
        Role defaultRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new RuntimeException("Role USER not found"));
        user.setRoles(new HashSet<>(Collections.singleton(defaultRole)));

        return user;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
