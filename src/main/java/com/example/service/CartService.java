package com.example.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.dao.CartDao;
import com.example.model.CartItem;

@Service
public class CartService {

    @Autowired
    private CartDao cartDao;

    public int addToCart(Long productId, int quantity, String sizeName, Long userId) {
        return cartDao.addToCart(productId, quantity, sizeName, userId);
    }

    public int countItemsByUser(Long userId) {
        return cartDao.countItemsByUser(userId);
    }
    public List<CartItem> getItemsByUser(Long userId) {
    return cartDao.findItemsByUser(userId);
}

    public void clearCart(Long userId) {
    cartDao.deleteItemsByUser(userId);
}

}