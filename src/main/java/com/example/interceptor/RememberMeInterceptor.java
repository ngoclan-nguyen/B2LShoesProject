package com.example.interceptor;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.model.RememberMeToken;
import com.example.service.RememberMeService;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class RememberMeInterceptor implements HandlerInterceptor {
	@Autowired
    private RememberMeService rememberMeTokenService;

	@PostConstruct
    public void init() {
        System.out.println("RememberMeInterceptor bean đã được khởi tạo.");
    }
	
	@Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
    	System.out.println("Interceptor is being called"); // Thêm log tại đây để kiểm tra
    	
    	// Lấy session hiện tại, nhưng không tạo mới nếu chưa có (tham số false).
        HttpSession session = request.getSession(false);
        
        // CHỈ THỰC HIỆN KIỂM TRA NẾU NGƯỜI DÙNG CHƯA ĐĂNG NHẬP
        if (session == null || session.getAttribute("currentAdmin") == null) {
        	// Lấy tất cả cookie từ trình duyệt gửi lên.
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("remember-me".equals(cookie.getName())) {
                        String token = cookie.getValue();
                        
                        // Dùng service để tìm token này trong cơ sở dữ liệu.
                        RememberMeToken rmt = rememberMeTokenService.findByToken(token);

                       // Kiểm tra xem token có tồn tại và còn hạn sử dụng không.
                        if (rmt != null && rmt.getExpiryDate().isAfter(LocalDateTime.now())) {
                            request.getSession(true).setAttribute("currentAdmin", rmt.getUser());
                        }
                    }
                }
            }
        }

        return true;
    }
}
