package com.baohanh.trungtambaohanh.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                // Cho phép truy cập công khai
                .requestMatchers(
                    new AntPathRequestMatcher("/"), 
                    new AntPathRequestMatcher("/login"), 
                    new AntPathRequestMatcher("/register"), 
                    new AntPathRequestMatcher("/register/save"),
                    // THÊM 2 DÒNG NÀY ĐỂ CHO PHÉP TRUY CẬP
                    new AntPathRequestMatcher("/forgot-password"),
                    new AntPathRequestMatcher("/reset-password"),
                    // ===================================
                    new AntPathRequestMatcher("/css/**"), 
                    new AntPathRequestMatcher("/js/**")
                ).permitAll()
                
                // Phân quyền theo vai trò (role)
                .requestMatchers(new AntPathRequestMatcher("/manager/**")).hasRole("Quản lý")
                .requestMatchers(new AntPathRequestMatcher("/technician/**")).hasRole("Kỹ thuật viên")
                .requestMatchers(new AntPathRequestMatcher("/receptionist/**")).hasRole("Nhân viên")
                .requestMatchers(new AntPathRequestMatcher("/customer/**")).hasRole("Khách hàng")
                
                // Tất cả các request khác đều cần phải xác thực
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/login?access_denied")
            )
            .userDetailsService(userDetailsService);
        
        return http.build();
    }
}