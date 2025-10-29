package com.example.clothesshop.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SellerAccessDeniedHandler implements AccessDeniedHandler {
    
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the access denied attempt for debugging
        System.out.println("=== SELLER ACCESS DENIED ===");
        System.out.println("User: " + (auth != null ? auth.getName() : "Anonymous"));
        System.out.println("Requested URL: " + request.getRequestURI());
        System.out.println("User authorities: " + (auth != null ? auth.getAuthorities() : "None"));
        System.out.println("========================");
        
        // Redirect to seller-specific access denied page
        response.sendRedirect("/access-denied");
    }
}
