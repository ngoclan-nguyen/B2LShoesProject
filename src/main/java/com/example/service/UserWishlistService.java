package com.example.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.UserWishlist;
import com.example.dao.UserWishlistDao;
import com.example.dto.UserWishlistDTO;

@Service
public class UserWishlistService {
	@Autowired
	private UserWishlistDao userWishlistDao;
	
	public List<UserWishlistDTO> getUserWishlistByUserId(Long userId) {
		return userWishlistDao.getUserWishlistByUserId(userId);
	}
	
	public boolean addUserWishlistByUserId(Long userId, Long productId) {
		return userWishlistDao.addUserWishlistByUserId(userId, productId);
	}

	public boolean removeUserWishlistByUserId(Long userId, Long productId) {
		return userWishlistDao.removeUserWishlistByUserId(userId,  productId);
	}
}