package com.example.clothesshop.config;

import com.example.clothesshop.service.CustomOAuth2UserService;
import com.example.clothesshop.service.impl.CustomUserDetailsService;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Configuration
public class SecurityConfig {
    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomOAuth2UserService oAuth2UserService;
    
    public SecurityConfig(CustomUserDetailsService uds, CustomAuthenticationSuccessHandler successHandler, CustomOAuth2UserService oAuth2UserService) { 
        this.userDetailsService = uds; 
        this.successHandler = successHandler;
        this.oAuth2UserService = oAuth2UserService;
    }

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

            // ✅ Configure OAuth2 login
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/home")
                .failureUrl("/login?error=oauth2")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oAuth2UserService)
                ))

            // ✅ Phân quyền truy cập
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/css/**", "/js/**", "/images/**",
                    "/register", "/login", "/", "/home", "/products", "/product/**",
                    "/about-us", "/contact", "/policy",
                    "/verify/**", "/verify-email", "/resend-verification",
                    "/forgot-password", "/reset-password",
                    "/seller/register"
                ).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/seller/**").hasRole("SELLER")
                .requestMatchers("/shipper/**").hasRole("SHIPPER")
                .anyRequest().authenticated()
            )

            // ✅ Cấu hình trang đăng nhập
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login") // URL xử lý login
                .usernameParameter("username") // Tên field username trong form
                .passwordParameter("password") // Tên field password trong form
                .successHandler(successHandler) // Sử dụng custom handler để redirect theo role
                .failureUrl("/login?error=true") // URL khi login fail
                .permitAll()
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