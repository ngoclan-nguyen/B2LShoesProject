package com.example.dao;

import com.example.model.CartItem;
import com.example.model.ProductVariant;
import com.example.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public class CartDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ProductDao productDao;

    // Hàm tiện ích để lấy Hibernate Session từ Spring
    private Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    public CartItem findByProductAndUser(Integer productVariantId, Long userId) {
        Session session = getCurrentSession();
        String hql = "FROM CartItem c WHERE c.productVariant.id = :vid AND c.user.id = :uid";
        Query<CartItem> query = session.createQuery(hql, CartItem.class);

        query.setParameter("vid", productVariantId);
        query.setParameter("uid", userId);

        return query.uniqueResult();
    }

    @Transactional // Spring sẽ tự begin transaction và commit khi hàm chạy xong
    public int addToCart(Long productId, int quantity, String sizeName, Long userId) {
        Session session = getCurrentSession();

        ProductVariant variant;
        if (sizeName != null && !sizeName.isEmpty()) {
            variant = productDao.findVariantByProductAndSize(productId, sizeName);
        } else {
            variant = productDao.findFirstVariantByProductId(productId);
        }

        if (variant == null) return -2;
        if (variant.getQuantity() < quantity) return -3;

        User userRef = session.load(User.class, userId); // Hibernate dùng load thay vì getReference

        CartItem existingItem = findByProductAndUser(variant.getId(), userId);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            existingItem.setUpdatedAt(LocalDateTime.now());
            session.update(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUser(userRef);
            newItem.setProductVariant(variant);
            newItem.setQuantity(quantity);

            newItem.setCreatedAt(LocalDateTime.now());
            newItem.setUpdatedAt(LocalDateTime.now());

            session.save(newItem);
        }

        return countItemsByUser(userId);
    }

    public int countItemsByUser(Long userId) {
        Session session = getCurrentSession();
        String hql = "SELECT SUM(c.quantity) FROM CartItem c WHERE c.user.id = :uid";
        Query<Long> query = session.createQuery(hql, Long.class);
        query.setParameter("uid", userId);
        Long result = query.uniqueResult();
        return result != null ? result.intValue() : 0;
    }
}