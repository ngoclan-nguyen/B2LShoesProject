package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cấu hình này nói rằng:
        // Mọi đường dẫn bắt đầu bằng /uploads/** (bao gồm cả /uploads/reviews/...)
        // Sẽ được tìm trong thư mục vật lý "uploads" nằm ngay gốc dự án
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}