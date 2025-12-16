package com.example.controller;

import com.example.model.OrderWeb;
import com.example.service.OrderService;
import com.example.service.VoucherService;
import com.example.dto.UserDTO;
import com.example.dto.VoucherDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/customer")
public class CustomerProfileController {

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/profile")
    public String profile(HttpServletRequest request) {return "customer/profile";}

    @GetMapping("/vouchers")
    public String listUserVouchers(
            @SessionAttribute(name = "currentCustomer", required = false) UserDTO userDto,
            Model model) {

        if (userDto == null) {
            // Chuyển hướng đến trang đăng nhập nếu chưa đăng nhập
            return "redirect:/login";
        }

        try {
            Long userId = userDto.getId();

            List<VoucherDTO> availableVouchers = voucherService.getAvailableVouchersForUser(userId);

            model.addAttribute("vouchers", availableVouchers);

        } catch (Exception e) {
            System.err.println("Lỗi khi tải ví voucher: " + e.getMessage());
            model.addAttribute("vouchers", List.of());
        }

        return "customer/voucher";
    }

    @GetMapping("/orders")
    public String showOrderHistory(
            @SessionAttribute(name = "currentCustomer", required = false) UserDTO userDto,
            Model model) {

        if (userDto == null) {
            // Chuyển hướng về trang đăng nhập nếu chưa login
            return "redirect:/login";
        }

        try {
            Long customerId = userDto.getId();
            // Lấy danh sách đơn hàng của khách hàng (sắp xếp theo ngày mới nhất)
            List<OrderWeb> orders = orderService.getOrdersByCustomerId(customerId);

            model.addAttribute("orders", orders);
            model.addAttribute("pageTitle", "Lịch sử Đơn hàng");

        } catch (Exception e) {
            model.addAttribute("orders", Collections.emptyList());
            // Log lỗi
            e.printStackTrace();
        }

        return "customer/order_history";
    }
}