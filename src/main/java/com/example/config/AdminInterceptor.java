package com.example.config;

import com.example.model.RememberMeToken;
import com.example.service.RememberMeService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Autowired
    private RememberMeService rememberMeService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession();

        // 1. Nếu đã đăng nhập (có session) -> Cho qua
        if (session.getAttribute("currentAdmin") != null) {
            return true;
        }

        // 2. [MỚI] Nếu mất Session -> Kiểm tra Cookie "remember-me-admin"
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("remember-me-admin".equals(cookie.getName())) {
                    String token = cookie.getValue();

                    // Kiểm tra token trong DB
                    RememberMeToken rmt = rememberMeService.findByToken(token);

                    // Nếu Token đúng và còn hạn
                    if (rmt != null && rmt.getExpiryDate().isAfter(LocalDateTime.now())) {
                        // Tự động đăng nhập lại (Hồi phục session)
                        session.setAttribute("currentAdmin", rmt.getUser());
                        return true;
                    }
                }
            }
        }

        // 3. Không có Session, không có Cookie -> Đá về Login
        response.sendRedirect("/admin/login");
        return false;
    }
}