package com.group6.clothingstore.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Tài nguyên tĩnh và các trang công khai
                        .requestMatchers(
                                "/",
                                "/shop/**",
                                "/register",
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",
                                "/error"
                        ).permitAll()

                        // Phân quyền Admin (NFR03, ERR-13)
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Các trang cần đăng nhập
                        .requestMatchers("/profile/**", "/cart/**", "/checkout/**", "/my-orders/**").authenticated()

                        // Các request còn lại
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        // Xử lý sau khi đăng nhập thành công: Phân luồng chuyển hướng
                        .successHandler((request, response, authentication) -> {
                            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
                            List<String> roles = authorities.stream().map(GrantedAuthority::getAuthority).toList();

                            log.info("Đăng nhập thành công: {}, Quyền: {}", userDetails.getUsername(), roles);

                            // Admin chuyển sang trang quản trị Dashboard, Customer chuyển về trang chủ
                            String redirectUrl = roles.contains("ROLE_ADMIN") ? "/admin/dashboard" : "/";
                            response.sendRedirect(request.getContextPath() + redirectUrl);
                        })
                        // Xử lý khi đăng nhập thất bại: Bắt chính xác lỗi sai pass (ERR-03) hoặc tài khoản bị khóa (ERR-04)
                        .failureHandler((request, response, exception) -> {
                            log.warn("Đăng nhập thất bại: {}", exception.getMessage());
                            if (exception instanceof LockedException) {
                                response.sendRedirect(request.getContextPath() + "/login?locked");
                            } else {
                                response.sendRedirect(request.getContextPath() + "/login?error");
                            }
                        })
                )
                // Ghi nhớ đăng nhập trong 14 ngày
                .rememberMe(remember -> remember
                        .key("clothing-store-secret-remember-me-key")
                        .rememberMeParameter("rememberMe")
                        .tokenValiditySeconds(14 * 24 * 60 * 60)
                        .userDetailsService(userDetailsService)
                )
                // Đăng xuất
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                )
                // Xử lý chặn truy cập trái phép
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authEx) ->
                                response.sendRedirect(request.getContextPath() + "/login?required")
                        )
                        .accessDeniedHandler((request, response, accessDeniedEx) ->
                                response.sendRedirect(request.getContextPath() + "/login?accessDenied")
                        )
                );

        return http.build();
    }
}