package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((requests) -> requests
                // Yêu cầu quyền ADMIN cho tất cả các trang bắt đầu bằng /admin/
                .requestMatchers("/admin/**").hasRole("ADMIN") 
                
                // Cho phép TẤT CẢ MỌI NGƯỜI truy cập các trang này
                .requestMatchers("/", "/home", "/css/**", "/images/**").permitAll() 
                
                // Bất kỳ yêu cầu nào khác đều cần phải đăng nhập (xác thực)
                .anyRequest().authenticated() 
            )
            .formLogin((form) -> form
                // Sử dụng trang đăng nhập mặc định của Spring Security
                .loginPage("/login") 
                .permitAll()
            )
            .logout((logout) -> logout.permitAll());

        return http.build();
    }
}