package com.example.controller;

import com.example.dto.UserDTO;
import com.example.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherApiController {

    @Autowired
    private VoucherService voucherService; // Giả định

    @PostMapping("/apply")
    public ResponseEntity<Map<String, Object>> applyVoucher(
            @SessionAttribute("currentCustomer") UserDTO userDto,
            @RequestBody Map<String, Object> requestBody) {

        String code = (String) requestBody.get("voucherCode");
        Long subTotal = ((Number) requestBody.get("subTotal")).longValue();

        // Bạn có thể lấy list variantIds nếu cần xác thực theo sản phẩm
        // List<Long> variantIds = (List<Long>) requestBody.get("variantIds");

        Map<String, Object> response = new HashMap<>();

        try {
            long discountAmount = voucherService.calculateDiscount(
                    userDto.getId(), // ID khách hàng
                    code,            // Mã voucher
                    subTotal         // Tổng tiền hàng
            );

            response.put("success", true);
            response.put("discountAmount", discountAmount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("discountAmount", 0L);
            return ResponseEntity.status(400).body(response);
        }
    }
}