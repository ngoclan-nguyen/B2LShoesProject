package com.example.controller;

import com.example.dto.OrderRequestDTO;
import com.example.dto.UserDTO;
import com.example.model.User;
import com.example.service.OrderService;
import com.example.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderRestController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserDao userDao;

    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(
            @SessionAttribute(name = "currentCustomer", required = false) UserDTO userDto,
            @RequestBody OrderRequestDTO orderRequest) {

        if (userDto == null) {
            Map<String, String> err = new HashMap<>();
            err.put("status", "error");
            err.put("message", "Vui lòng đăng nhập.");
            return ResponseEntity.status(401).body(err);
        }

        try {
            User customer = userDao.findUserById(userDto.getId());

            if (customer == null) {
                throw new Exception("Không tìm thấy khách hàng.");
            }

            Long orderId = orderService.placeOrder(customer, orderRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Đặt hàng thành công!");
            response.put("orderId", orderId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> err = new HashMap<>();
            err.put("status", "error");
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }
}