package com.example.clothesshop.service;

import com.example.clothesshop.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User save(User user);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<User> findById(Long id);
    List<User> findAll();
    void deleteById(Long id);
}
