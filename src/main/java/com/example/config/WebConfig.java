package com.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.interceptor.RememberMeInterceptor;
import com.example.service.RememberMeService;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private RememberMeInterceptor rememberMeInterceptor;

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Áp dụng quy tắc này cho tất cả các API có đường dẫn bắt đầu bằng /api/
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Cho phép các phương thức HTTP này
                .allowedHeaders("*") // Cho phép tất cả các header
                .allowCredentials(true); // Cho phép gửi cookie hoặc thông tin xác thực
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**") // Chặn tất cả trang Admin
                .excludePathPatterns( // [QUAN TRỌNG] Loại trừ các link này
                        "/admin/login",
                        "/admin/loginProcess",
                        "/admin/forget-pass-view", // Form nhập email
                        "/admin/forget-pass",      // Xử lý gửi OTP
                        "/admin/verify-code",      // Check OTP
                        "/admin/change-password",  // Đổi pass
                        "/admin/logout",
                        "/admin/assets/**",        // Tài nguyên tĩnh (nếu có)
                        "/admin/404"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String projectPath = Paths.get(".").toAbsolutePath().normalize().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/" + projectPath + "/uploads/");
    }
}