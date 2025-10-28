package com.example.clothesshop.security;

import com.example.clothesshop.model.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomOAuth2User implements OAuth2User {
    private final OAuth2User oauth2User;
    private final Set<Role> additionalRoles;

    public CustomOAuth2User(OAuth2User oauth2User, Set<Role> additionalRoles) {
        this.oauth2User = oauth2User;
        this.additionalRoles = additionalRoles;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Combine OAuth2User's original authorities with additional roles from database
        return Stream.concat(
            oauth2User.getAuthorities().stream(),
            additionalRoles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
        ).collect(Collectors.toSet());
    }

    @Override
    public String getName() {
        return oauth2User.getName();
    }
}