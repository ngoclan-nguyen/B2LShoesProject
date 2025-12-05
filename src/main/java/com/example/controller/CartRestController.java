package com.example.controller;

import com.example.dto.UserDTO;
import com.example.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartRestController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(required = false) String size,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        UserDTO userDto = (UserDTO) request.getSession().getAttribute("currentCustomer");
        if (userDto == null) {
            response.put("status", "error");
            response.put("message", "Bạn cần đăng nhập để mua hàng!");
            return ResponseEntity.ok(response);
        }

        int result = cartService.addToCart(productId, quantity, size, userDto.getId());

        if (result == -2) {
            response.put("status", "error");
            response.put("message", "Sản phẩm/Size này tạm hết hàng!");
        } else if (result == -3) {
            response.put("status", "error");
            response.put("message", "Kho không đủ số lượng yêu cầu!");
        } else if (result == -4) {
            response.put("status", "error");
            response.put("message", "Lỗi hệ thống, vui lòng thử lại!");
        } else {
            response.put("status", "success");
            response.put("message", "Đã thêm vào giỏ hàng!");
            response.put("totalItems", result);
        }

        return ResponseEntity.ok(response);
    }

    // API lấy số lượng sản phẩm trong giỏ
    @GetMapping("/count")
    public ResponseEntity<Integer> getCartCount(HttpServletRequest request) {
        UserDTO userDto = (UserDTO) request.getSession().getAttribute("currentCustomer");

        if (userDto == null) {
            return ResponseEntity.ok(0);
        }

        int count = cartService.countItemsByUser(userDto.getId());

        return ResponseEntity.ok(count);
    }
}