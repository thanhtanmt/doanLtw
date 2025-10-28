package com.example.clothesshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.clothesshop.model.User;
import com.example.clothesshop.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        // Get email from Google account
        String email = oauth2User.getAttribute("email");
        
        // Check if user exists in database
        if (!userRepository.existsByEmail(email)) {
            throw new OAuth2AuthenticationException("Email " + email + " not registered in system");
        }
        
        // Get user from database to get their roles
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new OAuth2AuthenticationException("User not found for email: " + email));

        // Convert roles to authorities
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
            System.out.println("Added authority: " + role.getName());
        });
        
        // Add existing OAuth2User authorities
        oauth2User.getAuthorities().forEach(authority -> {
            authorities.add(new SimpleGrantedAuthority(authority.getAuthority()));
        });

        System.out.println("Final authorities for user: " + authorities);

        // Create new OAuth2User with combined authorities
        return new DefaultOAuth2User(
            authorities,
            oauth2User.getAttributes(),
            "email"  // Use email as the name attribute key
        );
    }
}