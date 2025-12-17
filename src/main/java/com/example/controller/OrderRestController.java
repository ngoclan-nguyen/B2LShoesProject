package com.example.controller;

import com.example.dto.CheckoutFormDTO;
import com.example.dto.UserDTO;
import com.example.model.OrderWeb;
import com.example.service.OrderService;
import com.example.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderRestController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private VNPayService vnpayService;

    @PostMapping("/place")
    public ResponseEntity<Map<String, Object>> placeOrder(
            @SessionAttribute(name = "currentCustomer", required = false) UserDTO userDto,
            @ModelAttribute CheckoutFormDTO checkoutForm,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        // 1. Xử lý lỗi 401: Người dùng chưa đăng nhập
        if (userDto == null) {
            response.put("status", "error");
            response.put("message", "Vui lòng đăng nhập để tiếp tục đặt hàng.");
            return ResponseEntity.status(401).body(response); // Unauthorized
        }

        try {
            OrderWeb newOrder = orderService.placeOrder(userDto, checkoutForm);

            response.put("status", "success");
            response.put("orderId", newOrder.getId());
            response.put("paymentMethod", newOrder.getPaymentMethod());

            if ("TRANSFER".equalsIgnoreCase(newOrder.getPaymentMethod())) {
                // TẠO URL VNPAY
                String vnpayUrl = vnpayService.createPaymentUrl(newOrder, request);
                response.put("vnpayUrl", vnpayUrl);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 4. Xử lý lỗi nghiệp vụ (ví dụ: Giỏ hàng trống, không đủ tồn kho, lỗi xác thực giá)
            e.printStackTrace(); // Nên sử dụng Logger thay vì System.err

            response.put("status", "error");
            response.put("message", e.getMessage()); // Trả về thông báo lỗi nghiệp vụ

            return ResponseEntity.badRequest().body(response); // 400 Bad Request
        }
    }
    @GetMapping("/re-payment-url")
    public ResponseEntity<Map<String, Object>> getRePaymentUrl(
            @RequestParam("orderId") Long orderId,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        try {
            // 1. Tìm đơn hàng cũ trong DB
            OrderWeb order = orderService.getOrderById(orderId);

            // 2. Kiểm tra điều kiện: Đơn hàng tồn tại, chưa thanh toán và dùng phương thức chuyển khoản
            if (order != null && "PENDING".equals(order.getPaymentStatus())
                    && "TRANSFER".equals(order.getPaymentMethod())) {

                // 3. Tạo lại URL VNPay mới (với cùng số tiền và mã đơn đó)
                String vnpayUrl = vnpayService.createPaymentUrl(order, request);

                response.put("status", "success");
                response.put("vnpayUrl", vnpayUrl);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Đơn hàng không đủ điều kiện thanh toán lại.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}