package com.baohanh.trungtambaohanh.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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
                    "/", 
                    "/login", 
                    "/register", 
                    "/register/save",
                    "/css/**", 
                    "/js/**", 
                    "/images/**", 
                    "/webjars/**",
                    "/favicon.ico"
                ).permitAll()
                
                // Phân quyền theo role
                .requestMatchers("/manager/**").hasRole("Quản lý")
                .requestMatchers("/technician/**").hasRole("Kỹ thuật viên")
                .requestMatchers("/receptionist/**").hasRole("Nhân viên")
                .requestMatchers("/customer/**").hasRole("Khách hàng")
                
                // Các request khác cần xác thực
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
            .userDetailsService(userDetailsService)
            .headers(headers -> headers
                .contentTypeOptions(contentType -> contentType.disable())
            );
        
        return http.build();
    }
}