package com.example.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Set;

@Configuration
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 1. Lấy danh sách quyền (Role) của user vừa đăng nhập
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        // 2. Kiểm tra Role và chuyển hướng
        if (roles.contains("ROLE_ADMIN") || roles.contains("Admin")) {
            response.sendRedirect("/admin/products"); // Admin -> Vào trang quản lý sản phẩm
        } else {
            // Nếu có redirectUrl (ví dụ từ trang thêm giỏ hàng) thì ưu tiên quay lại đó
            String redirectUrl = request.getParameter("redirect");
            if (redirectUrl != null && !redirectUrl.isEmpty()) {
                response.sendRedirect(redirectUrl);
            } else {
                response.sendRedirect("/"); // Khách -> Về trang chủ
            }
        }
    }
}