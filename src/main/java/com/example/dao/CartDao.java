package com.example.dao;

import com.example.model.CartItem;

import com.example.config.HibernateUtil;

import com.example.model.ProductVariant;
import com.example.model.User;
import com.example.dto.UserCartItemDTO;
import com.example.dto.UserWishlistDTO;
import com.example.model.ProductVariant;
import com.example.model.User;
import com.example.model.UserWishlist;

import java.math.BigDecimal;
import java.util.List;


import com.example.model.ProductVariant;
import com.example.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.Transaction;
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
    
    public List<UserCartItemDTO> getCartItemByUserId(Long userId) {
		List<UserCartItemDTO> userCartItem = null;
		Session session = null;
		Transaction transaction = null;
		try {
			session = HibernateUtil.getSession();
			transaction = session.beginTransaction();
			
			String hql = "SELECT new com.example.dto.UserCartItemDTO("
					+ "c.id, c.createdAt, c.updatedAt, c.quantity, c.user.id, p.id, pv.id, p.name, p.price, pv.quantity, s.sizeName, img.path) "
					+ "FROM CartItem c "
					+ "LEFT JOIN c.productVariant pv "
					+ "LEFT JOIN pv.size s "
					+ "LEFT JOIN pv.product p "
					+ "LEFT JOIN p.productImages img "
					+ "WHERE c.user.id = :uid "
					+ "AND img.isPrimary = true";
			Query<UserCartItemDTO> query = session.createQuery(hql, UserCartItemDTO.class);
			query.setParameter("uid", userId);
			
			userCartItem = query.list();
			transaction.commit();
		} catch (Exception e) {
			if (transaction != null) transaction.rollback();
		 	e.printStackTrace();
		} finally {
			if (session != null) session.close();
		}
		return userCartItem;
    }
    
    public Long getTotalAmountBySelectedItem(Long userId, List<Long> productVariantIds) {
    	if(productVariantIds == null ||  productVariantIds.isEmpty()) {
    		return 0L;
    	}
    	
    	Long totalAmount = 0L;
    	Session session = null;
    	Transaction transaction = null;
    	try {
    		session = HibernateUtil.getSession();
    		transaction = session.beginTransaction();
    		
    		String hql = "SELECT SUM(p.price * c.quantity) "
    				+ "FROM CartItem c "
    				+ "JOIN c.productVariant pv "
    				+ "JOIN pv.product p "
    				+ "WHERE c.user.id = :uid "
    				+ "AND pv.id IN :pvids";
    		Query<Long> query = session.createQuery(hql, Long.class);
    		query.setParameter("uid", userId);
    		query.setParameter("pvids", productVariantIds);

    		totalAmount = query.uniqueResult();
    		if (totalAmount == null) return 0L;
    		
    		transaction.commit();
    	} catch (Exception e) {
			if (transaction != null) transaction.rollback();
		 	e.printStackTrace();
		} finally {
			if (session != null) session.close();
		}
		return totalAmount;
    }
    
    @Transactional
    public int removeCartItem(Long userId, Integer productVariantId) {
		Session session = getCurrentSession();
		
		String hql = "FROM CartItem c WHERE c.user.id = :uid AND c.productVariant.id = :pvid";
	    CartItem itemToDelete = session.createQuery(hql, CartItem.class)
	            .setParameter("uid", userId)
	            .setParameter("pvid", productVariantId)
	            .uniqueResult();

	    if (itemToDelete != null) {
	        session.remove(itemToDelete); // Xóa CartItem
	    }

	    return countItemsByUser(userId);
	}
    
    @Transactional
    public int updateQuantity(Long userId, Long productVariantId, Integer quantity) {
        Session session = getCurrentSession();

        // 1. Cập nhật số lượng CartItem
	    String hqlUpdate = "UPDATE CartItem c "
	                     + "SET c.quantity = :quantity "
	                     + "WHERE c.user.id = :uid "
	                     + "AND c.productVariant.id = :pvid";
	
	    @SuppressWarnings("deprecation")
		Query<?> query = session.createQuery(hqlUpdate);
	    query.setParameter("uid", userId);
	    query.setParameter("pvid", productVariantId);
	    query.setParameter("quantity", quantity);

        int rowsAffected = query.executeUpdate();
        System.out.println("Rows updated: " + rowsAffected);

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
	public Long calculateTotalAmount(Long userId, List<Long> productVariantIds) {
		Session session = null;
		Transaction transaction = null;
		Long total = 0L;

		try {
			session = HibernateUtil.getSession();
			transaction = session.beginTransaction();

			String hql = "SELECT SUM(p.price * c.quantity) " +
					"FROM CartItem c " +
					"JOIN c.productVariant v " +
					"JOIN v.product p " +
					"WHERE c.user.id = :userId " +
					"AND v.id IN (:variantIds)";

			Query<Long> query = session.createQuery(hql, Long.class);
			query.setParameter("userId", userId);
			query.setParameter("variantIds", productVariantIds);

			total = query.uniqueResult();

			transaction.commit();
		} catch (Exception e) {
			if (transaction != null) transaction.rollback();
			e.printStackTrace();
		} finally {
			if (session != null && session.isOpen()) session.close();
		}

		return total != null ? total : 0L;
	}

	public CartItem getCartItemByUserAndVariant(Long userId, Long variantId) {
		Session session = null;
		Transaction transaction = null;
		CartItem item = null;
		try {
			session = HibernateUtil.getSession();
			transaction = session.beginTransaction();
			String hql = "FROM CartItem c WHERE c.user.id = :userId AND c.productVariant.id = :variantId";

			Query<CartItem> query = session.createQuery(hql, CartItem.class);
			query.setParameter("userId", userId);
			query.setParameter("variantId", variantId);
			item = query.uniqueResult();
			transaction.commit();
		} catch (Exception e) {
			if (transaction != null) transaction.rollback();
			e.printStackTrace();
		} finally {
			if (session != null && session.isOpen()) session.close();
		}

		return item;
	}
	public void deleteCartItem(Long userId, Long variantId) {
		Session session = null;
		Transaction transaction = null;
		try {
			session = HibernateUtil.getSession();
			transaction = session.beginTransaction();

			String hql = "DELETE FROM CartItem c WHERE c.user.id = :userId AND c.productVariant.id = :variantId";

			session.createQuery(hql)
					.setParameter("userId", userId)
					.setParameter("variantId", variantId)
					.executeUpdate();

			transaction.commit();
		} catch (Exception e) {
			if (transaction != null) transaction.rollback();
			e.printStackTrace();
		} finally {
			if (session != null && session.isOpen()) session.close();
		}
	}
}

