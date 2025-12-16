package com.example.controller;

import com.example.dto.UserDTO;
import com.example.model.OrderWeb;
import com.example.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import com.example.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping("/customer")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/orders/{orderId}")
    public String showOrderDetail(
            @PathVariable("orderId") Long orderId,
            @SessionAttribute(name = "currentCustomer", required = false) UserDTO userDto,
            HttpServletRequest request) {

        if (userDto == null) {
            return "redirect:/login";
        }

        Long customerId = userDto.getId();
        OrderWeb order = orderService.getOrderDetail(orderId, customerId);

        if (order == null) {
            return "redirect:/customer/orders?error=OrderNotFound";
        }

        request.setAttribute("order", order);
        request.setAttribute("pageTitle", "Chi tiết Đơn hàng #" + orderId);

        return "customer/order_detail";
    }
}
