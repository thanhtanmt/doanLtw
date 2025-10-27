package com.example.clothesshop.config;

import com.example.clothesshop.service.impl.CustomUserDetailsService;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy; 

@Configuration
public class SecurityConfig {
    private final CustomUserDetailsService userDetailsService;
    public SecurityConfig(CustomUserDetailsService uds) { this.userDetailsService = uds; }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ✅ Luôn tạo session để CSRF token có chỗ lưu
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
            )

            // ✅ Bật CSRF (để Spring sinh token) — bạn có thể thêm ignore nếu cần
            .csrf(csrf -> csrf
            	    .ignoringRequestMatchers("/h2-console/**", "/verify-email", "/verify/**", "/resend-verification")
            	)

            // ✅ Phân quyền truy cập
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/css/**", "/js/**", "/images/**",
                    "/register", "/login", "/", "/home", "/products",
                    "/verify/**", "/verify-email", "/resend-verification"
                ).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN") 
                .anyRequest().authenticated()
            )

            // ✅ Cấu hình trang đăng nhập
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
                .defaultSuccessUrl("/", true)
            )

            // ✅ Cấu hình đăng xuất
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//            .csrf().disable()  // ✅ tắt kiểm tra CSRF
//            .authorizeHttpRequests(auth -> auth
//                .anyRequest().permitAll() // ✅ cho phép tất cả truy cập
//            )
//            .formLogin(form -> form.disable()) // ✅ tắt login form
//            .logout(logout -> logout.disable()); // ✅ tắt logout
//        return http.build();
//    }

}