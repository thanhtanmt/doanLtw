package com.example.clothesshop.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String requestURI = request.getRequestURI();
        
        // Log the access denied attempt for debugging
        System.out.println("=== ACCESS DENIED ===");
        System.out.println("User: " + (auth != null ? auth.getName() : "Anonymous"));
        System.out.println("Requested URL: " + requestURI);
        System.out.println("User authorities: " + (auth != null ? auth.getAuthorities() : "None"));
        System.out.println("====================");
        
        // Check if the request is for seller pages
        if (requestURI.startsWith("/seller")) {
            // Redirect to seller-specific access denied page
            response.sendRedirect("/access-denied");
            return;
        }
        
        // For admin pages or other protected resources
        if (auth != null && auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            // If the user is an admin trying to access something they shouldn't
            response.sendRedirect("/admin/dashboard");
        } else {
            // For other users
            response.sendRedirect("/?error=access-denied");
        }
    }
}
