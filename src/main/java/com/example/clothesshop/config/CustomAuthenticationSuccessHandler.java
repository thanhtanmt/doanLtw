package com.example.clothesshop.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        System.out.println("========================================");
        System.out.println("🔐 Login Success Handler");
        System.out.println("Username: " + authentication.getName());
        System.out.println("Authorities: " + authentication.getAuthorities());
        
        // Kiểm tra role của user và redirect đến trang tương ứng
        String redirectUrl = "/home"; // Mặc định cho ROLE_USER
        
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            redirectUrl = "/admin/dashboard";
            System.out.println("✅ Detected ROLE_ADMIN -> Redirect to: " + redirectUrl);
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SELLER"))) {
            redirectUrl = "/seller";
            System.out.println("✅ Detected ROLE_SELLER -> Redirect to: " + redirectUrl);
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SHIPPER"))) {
            redirectUrl = "/shipper/dashboard";
            System.out.println("✅ Detected ROLE_SHIPPER -> Redirect to: " + redirectUrl);
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"))) {
            redirectUrl = "/home";
            System.out.println("✅ Detected ROLE_USER -> Redirect to: " + redirectUrl);
        } else {
            System.out.println("⚠️ No matching role found, using default: " + redirectUrl);
        }
        
        System.out.println("========================================");
        response.sendRedirect(redirectUrl);
    }
}
