package com.example.clothesshop.config;

import com.example.clothesshop.service.CustomOAuth2UserService;
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
    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomOAuth2UserService oAuth2UserService;
    private final AdminAccessDeniedHandler adminAccessDeniedHandler;
    
    public SecurityConfig(CustomAuthenticationSuccessHandler successHandler, 
                        CustomOAuth2UserService oAuth2UserService,
                        AdminAccessDeniedHandler adminAccessDeniedHandler) {
        this.successHandler = successHandler;
        this.oAuth2UserService = oAuth2UserService;
        this.adminAccessDeniedHandler = adminAccessDeniedHandler;
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
                .defaultSuccessUrl("/admin/dashboard") // Change default success URL for admin
                .failureUrl("/login?error=oauth2")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oAuth2UserService)
                ))

            // ✅ Phân quyền truy cập
            .authorizeHttpRequests(auth -> auth
                // Public resources and pages
                .requestMatchers(
                    "/css/**", "/js/**", "/images/**",
                    "/register", "/login", 
                    "/verify/**", "/verify-email", "/resend-verification",
                    "/forgot-password", "/reset-password",
                    "/seller/register"
                ).permitAll()
                // Admin paths
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Other paths that admin should not access
                .requestMatchers("/", "/home", "/products", "/product/**",
                    "/about-us", "/contact", "/policy",
                    "/cart/**", "/checkout/**", "/wishlist/**").hasAnyRole("USER", "SELLER", "SHIPPER")
                .requestMatchers("/seller/**").hasRole("SELLER")
                .requestMatchers("/shipper/**").hasRole("SHIPPER")
                .anyRequest().authenticated()
            )

            // ✅ Cấu hình trang đăng nhập
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )

            // ✅ Cấu hình đăng xuất
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            )

            // ✅ Configure access denied handler
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(adminAccessDeniedHandler)
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