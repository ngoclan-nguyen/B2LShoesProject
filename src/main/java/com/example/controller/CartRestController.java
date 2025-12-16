package com.example.controller;

import com.example.dto.UserDTO;
import com.example.dto.VoucherApplyResultDTO;
import com.example.service.CartService;
import com.example.service.VoucherService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartRestController {

    @Autowired
    private CartService cartService;
    @Autowired
    private VoucherService voucherService;

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(required = false) String size,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        UserDTO userDto = (UserDTO) request.getSession().getAttribute("currentCustomer");
        if (userDto == null) {
            return ResponseEntity.ok(Map.of("status", "error", "message", "auth_required"));
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
    
    @PostMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeCartItem(
            HttpServletRequest request, 
            @RequestParam Integer productVariantId) {

        Map<String, Object> response = new HashMap<>();
        UserDTO user = (UserDTO) request.getSession().getAttribute("currentCustomer");

        if (user == null) {
            response.put("status", "error");
            response.put("message", "auth_required");
            return ResponseEntity.ok(response);
        }

        int totalItems = cartService.removeCartItem(user.getId(), productVariantId);

        if (totalItems >= 0) {
            response.put("status", "success");
            response.put("message", "Đã xóa sản phẩm khỏi giỏ hàng!");
            response.put("totalItems", totalItems);
        } else {
            response.put("status", "error");
            response.put("message", "Xóa thất bại, vui lòng thử lại!");
        }

        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/updateSelection")
    @ResponseBody 
    public Map<String, Object> updateCartSelection(HttpServletRequest request, 
    			@RequestBody(required = false) List<Long> productVariantIds) {
    	UserDTO user = (UserDTO)request.getSession().getAttribute("currentCustomer");
		Long userId = null;
		if (user != null)
			userId = user.getId(); 
    	request.getSession().setAttribute("productVariantIds", productVariantIds);
    	
    	Long totalAmount = cartService.getTotalAmountBySelectedItem(userId, productVariantIds);
    	
    	return Map.of("totalAmount", totalAmount != null ? totalAmount : 0L);
    }
    
    @PostMapping("/updateQuantity")
    @ResponseBody
    public Map<String, Object> updateQuantity(@RequestParam Long productVariantId,
                                              @RequestParam Integer quantity,
                                              HttpServletRequest request) {
    	Map<String, Object> response = new HashMap<>();

        UserDTO user = (UserDTO) request.getSession().getAttribute("currentCustomer");
        if (user == null) {
            response.put("status", "error");
            response.put("message", "auth_required");
            response.put("totalItems", 0);
            return response;
        }
        Long userId = user.getId();
        int totalItems = cartService.updateQuantity(userId, productVariantId, quantity);

        response.put("status", "success");
        response.put("message", "Cập nhật số lượng thành công!");
        response.put("totalItems", totalItems);

        return response;
    }

    @PostMapping("/applyVoucher")
    public ResponseEntity<Map<String, Object>> applyVoucher(
            @RequestParam String voucherCode,
            @RequestParam(required = false) List<Long> productVariantIds,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        UserDTO userDto = (UserDTO) request.getSession().getAttribute("currentCustomer");

        if (userDto == null) {
            response.put("status", "error");
            response.put("message", "Vui lòng đăng nhập để sử dụng mã khuyến mãi.");
            return ResponseEntity.ok(response);
        }

        // Gọi CartService/VoucherService để xử lý logic kiểm tra và tính toán
        try {
            // Hàm này sẽ kiểm tra tính hợp lệ và tính toán số tiền giảm giá
            VoucherApplyResultDTO result = voucherService.applyVoucher(
                    userDto.getId(),
                    voucherCode,
                    productVariantIds
            );

            response.put("status", "success");
            response.put("message", "Áp dụng mã khuyến mãi thành công!");
            response.put("originalAmount", result.getOriginalAmount());
            response.put("discountAmount", result.getDiscountAmount());
            response.put("finalAmount", result.getFinalAmount());
            response.put("data", result);

            // Lưu mã giảm giá vào session hoặc DB để sử dụng khi đặt hàng
            request.getSession().setAttribute("appliedVoucherCode", voucherCode);

        } catch (Exception e) {
            // Xử lý các lỗi nghiệp vụ: Hết hạn, không đủ điều kiện, mã không tồn tại...
            response.put("status", "error");
            response.put("message", e.getMessage()); // Truyền thông báo lỗi từ Service
        }

        return ResponseEntity.ok(response);
    }
    
}