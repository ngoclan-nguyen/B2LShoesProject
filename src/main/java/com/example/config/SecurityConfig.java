package com.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomSuccessHandler customSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF (để đơn giản hóa việc submit form)
                .csrf(Customizer.withDefaults())

                // Phân quyền
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/customer/**", "/vendor/**", "/uploads/**").permitAll()

                        // Cấu hình cho admin
                        .requestMatchers(
                                "/admin/login",
                                "/admin/loginProcess",      // Xử lý đăng nhập
                                "/admin/forget-pass-view",  // Xem form quên mật khẩu
                                "/admin/forget-pass",       // Xử lý gửi OTP
                                "/admin/verify-code",       // Xử lý check OTP
                                "/admin/change-password",   // Xử lý đổi pass
                                "/admin/logout"             // Đăng xuất
                        ).permitAll()
                        .requestMatchers("/admin/**").permitAll()

                        // Cấu hình cho customer
                        .requestMatchers("/**").permitAll()
                )

                // Cấu hình Đăng nhập (Form Login)
                .formLogin(form -> form
                        .loginPage("/login")                  // Trang login mặc định cho khách
                        .loginProcessingUrl("/perform-login") // URL xử lý khi bấm nút "Đăng nhập"
                        .successHandler(customSuccessHandler) // Dùng handler để phân luồng Admin/User
                        .failureUrl("/login?error=true")      // Đăng nhập sai thì về lại đây
                        .permitAll()
                )

                // Cấu hình logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/") // Đăng xuất xong về trang chủ
                        .permitAll()
                );

        return http.build();
    }
}