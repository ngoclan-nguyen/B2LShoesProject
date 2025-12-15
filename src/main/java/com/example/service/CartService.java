package com.example.service;

import com.example.dao.CartDao;
import com.example.dao.ProductDao;
import com.example.dao.UserDao;
import com.example.dto.UserCartItemDTO;
import com.example.model.CartItem;
import com.example.model.ProductVariant;
import com.example.model.User;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private CartDao cartDao;

    public int addToCart(Long productId, int quantity, String sizeName, Long userId) {
        return cartDao.addToCart(productId, quantity, sizeName, userId);
    }

    public int countItemsByUser(Long userId) {
        return cartDao.countItemsByUser(userId);
    }
    
    public List<UserCartItemDTO> getCartItemByUserId(Long userId) {
    	return cartDao.getCartItemByUserId(userId);
    }
    
    public Long getTotalAmountBySelectedItem(Long userId, List<Long> productVariantIds) {
    	return cartDao.getTotalAmountBySelectedItem(userId, productVariantIds);
    }
    
    public int removeCartItem(Long userId, Integer productVariantId) {
    	return cartDao.removeCartItem(userId, productVariantId);
    }
    
    public int updateQuantity(Long userId, Long productVariantId, Integer quantity) {
    	return cartDao.updateQuantity(userId, productVariantId, quantity);
    }
}