package com.example.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
	public ResponseEntity<?> addWishlist(HttpServletRequest request, @RequestParam Long productId) {
		UserDTO user = (UserDTO)request.getSession().getAttribute("currentCustomer");
		Long userId = null;
		if (user == null) {
			return ResponseEntity.badRequest().body("Please Login!");
		} else {
			userId = user.getId();
		}
		boolean addStatus = userWishlistService.addUserWishlistByUserId(userId, productId);
		return (addStatus) ? ResponseEntity.ok("Add Success!") : ResponseEntity.badRequest().body("Add Fail!");
	}
	
	@PostMapping("/remove")
	public ResponseEntity<?> removeWishlist(HttpServletRequest request, @RequestParam Long productId) {
		//UserDTO user = (UserDTO)request.getSession().getAttribute("currentCustomer");
		//Long userId = null;
		//if (user == null) {
		//	return ResponseEntity.badRequest().body("Please Login!");
		//} else {
		//	userId = user.getId();
		//}
		boolean deleteStatus = userWishlistService.removeUserWishlistByUserId(32L, productId);
		return (deleteStatus) ? ResponseEntity.ok("Delete Succes!") : ResponseEntity.badRequest().body("Delete Fail!");
	}
}