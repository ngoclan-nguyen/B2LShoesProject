package com.example.controller;

import com.example.dto.UserDTO;
import com.example.service.CartService;
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
    
    @PostMapping("/remove")
	public ResponseEntity<?> removeCartItem(HttpServletRequest request, @RequestParam Integer productVariantId) {
		//UserDTO user = (UserDTO)request.getSession().getAttribute("currentCustomer");
		//Long userId = null;
		//if (user == null) {
		//	return ResponseEntity.badRequest().body("Please Login!");
		//} else {
		//	userId = user.getId();
		//}
		boolean deleteStatus = cartService.removeCartItem(10L, productVariantId);
		return (deleteStatus) ? ResponseEntity.ok("Delete Succes!") : ResponseEntity.badRequest().body("Delete Fail!");
	}
    
    @PostMapping("/updateSelection")
    @ResponseBody 
    public Map<String, Object> updateCartSelection(HttpServletRequest request, 
    			@RequestBody(required = false) List<Long> productVariantIds) {
    	//UserDTO user = (UserDTO)request.getSession().getAttribute("currentCustomer");
		//Long userId = null;
		//if (user != null)
		//	userId = user.getId(); 
    	request.getSession().setAttribute("productVariantIds", productVariantIds);
    	
    	Long totalAmount = cartService.getTotalAmountBySelectedItem(6L, productVariantIds);
    	
    	return Map.of("totalAmount", totalAmount != null ? totalAmount : 0L);
    }
    
    @SuppressWarnings("unchecked")
	@PostMapping("/updateQuantity")
    @ResponseBody
    public Map<String, Object> updateQuantity(@RequestParam Long productVariantId,
                                              @RequestParam Integer quantity,
                                              HttpServletRequest request) {
    	//UserDTO user = (UserDTO)request.getSession().getAttribute("currentCustomer");
    			//Long userId = null;
    			//if (user != null)
    			//	userId = user.getId(); 
        Long userId = 6L;
        cartService.updateQuantity(userId, productVariantId, quantity);

		List<Long> selectedIds = (List<Long>) request.getSession().getAttribute("productVariantIds");

        Long totalAmount = cartService.getTotalAmountBySelectedItem(userId, selectedIds);

        return Map.of("totalAmount", totalAmount != null ? totalAmount : 0L);
    }
    
}