package com.example.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.dao.UserDao;
import com.example.dto.UserDTO;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
public class UserController {
	@Autowired 
	private UserDao userDao;
	
	@PostMapping("/update-profile")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> updateProfile(
	        @RequestParam String name,
	        @RequestParam String phone,
	        @RequestParam String address,
	        HttpServletRequest request) {

	    Map<String, Object> response = new HashMap<>();
	    UserDTO user = (UserDTO) request.getSession().getAttribute("currentCustomer");

	    if (user == null) {
	        response.put("status", "error");
	        response.put("message", "Vui lòng đăng nhập");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	    }

	    boolean updated = userDao.updateProfile(user.getId(), name, phone, address);

	    if(updated){
	        // cập nhật lại session với dữ liệu mới
	        user.setName(name);
	        user.setPhone(phone);
	        user.setAddress(address);

	        response.put("status", "success");
	        response.put("message", "Cập nhật thành công!");
	        response.put("data", user); // trả về dữ liệu mới
	    } else {
	        response.put("status", "error");
	        response.put("message", "Cập nhật thất bại!");
	    }

	    return ResponseEntity.ok(response);
	}
}
