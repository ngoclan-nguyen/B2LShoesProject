package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("home") // Đặt tiền tố /admin cho tất cả các request trong Controller này
public class HomeController {

    @GetMapping
    public String homePage() {
        return "/home";
    }
    
    // Thêm các trang khác như quản lý đơn hàng, quản lý user...
}