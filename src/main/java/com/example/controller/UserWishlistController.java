package com.example.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.UserDTO;
import com.example.service.UserWishlistService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("api/wishlist")
public class UserWishlistController {
	
	@Autowired 
	private UserWishlistService userWishlistService;
	
	@PostMapping("/add") 
	@ResponseBody
	public ResponseEntity<Map<String, Object>> addWishlist(HttpServletRequest request,
	                                                       @RequestParam Long productId) {
	    Map<String, Object> response = new HashMap<>();
	    UserDTO user = (UserDTO) request.getSession().getAttribute("currentCustomer");

	    if (user == null) {
	        response.put("status", "error");
	        response.put("message", "Vui lòng đăng nhập để sử dụng chức năng yêu thích!");
	        response.put("totalItems", 0);
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	    }

	    Long userId = user.getId();
	    boolean addStatus = userWishlistService.addUserWishlistByUserId(userId, productId);

	    if (addStatus) {
	        response.put("status", "success");
	        response.put("message", "Đã thêm vào yêu thích!");
	    } else {
	        response.put("status", "error");
	        response.put("message", "Thêm thất bại!");
	    }

	    return ResponseEntity.ok(response);
	}
	
	@PostMapping("/remove")
	public ResponseEntity<?> removeWishlist(HttpServletRequest request, @RequestParam Long productId) {
		UserDTO user = (UserDTO)request.getSession().getAttribute("currentCustomer");
		Long userId = null;
		if (user == null) {
			return ResponseEntity.badRequest().body("Please Login!");
		} else {
			userId = user.getId();
		}
		boolean deleteStatus = userWishlistService.removeUserWishlistByUserId(userId, productId);
		return (deleteStatus) ? ResponseEntity.ok("Delete Succes!") : ResponseEntity.badRequest().body("Delete Fail!");
	}
}