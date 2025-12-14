package com.example.dao;

import com.example.config.HibernateUtil;
import com.example.dto.UserWishlistDTO;
import com.example.model.MasterSize;
import com.example.model.Product;
import com.example.model.User;
import com.example.model.UserWishlist;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserWishlistDao {
	public List<UserWishlistDTO> getUserWishlistByUserId(Long userId) {
		List<UserWishlistDTO> userWishlist = null;
		Session session = null;
		Transaction transaction = null;
		try {
			session = HibernateUtil.getSession();
			transaction = session.beginTransaction();
			
			String hql = "SELECT new com.example.dto.UserWishlistDTO(uw.id, uw.user.Id, p.id, p.name, p.description, p.price, img.path, uw.createdAt, uw.updatedAt) "
					+ "FROM UserWishlist uw "
					+ "LEFT JOIN uw.product p "
					+ "LEFT JOIN uw.product.productImages img "
					+ "WHERE uw.user.id = :userId "
					+ "AND img.isPrimary = true";
			Query<UserWishlistDTO> query = session.createQuery(hql, UserWishlistDTO.class);
			query.setParameter("userId", userId);
			
			userWishlist = query.list();
			transaction.commit();
		} catch (Exception e) {
			if (transaction != null) transaction.rollback();
		 	e.printStackTrace();
		} finally {
			if (session != null) session.close();
		}
		return userWishlist;
	}
	
	public boolean addUserWishlistByUserId(Long userId, Long productId) {
	    Session session = null;
	    Transaction transaction = null;
	    try {
	        session = HibernateUtil.getSession();
	        transaction = session.beginTransaction();

	        User user = session.get(User.class, userId);
	        Product product = session.get(Product.class, productId);

	        if (user == null || product == null) {
	            return false;
	        }

	        String checkHql = "SELECT COUNT(uw) FROM UserWishlist uw WHERE uw.user.id = :uid AND uw.product.id = :pid";
	        Long count = session.createQuery(checkHql, Long.class)
	                            .setParameter("uid", userId)
	                            .setParameter("pid", productId)
	                            .uniqueResult();

	        if (count > 0) {
	            return false;
	        }

	        UserWishlist newItem = new UserWishlist();
	        newItem.setUser(user);     
	        newItem.setProduct(product); 
	        newItem.setCreatedAt(LocalDateTime.now());
	        newItem.setUpdatedAt(LocalDateTime.now()); 

	        session.persist(newItem);

	        transaction.commit();
	        return true;

	    } catch (Exception e) {
	        if (transaction != null) {
	            transaction.rollback();
	        }
	        return false;
	    } finally {
	        if (session != null && session.isOpen()) {
	            session.close();
	        }
	    }
	}
	
	public boolean removeUserWishlistByUserId(Long userId, Long productId) {
		Session session = null;
		Transaction transaction = null;
		try {
			session = HibernateUtil.getSession();
			transaction = session.beginTransaction();
			
	        String hql = "FROM UserWishlist uw WHERE uw.user.id = :uid AND uw.product.id = :pid";
	        UserWishlist itemToDelete = session.createQuery(hql, UserWishlist.class)
                    .setParameter("uid", userId)
                    .setParameter("pid", productId)
                    .uniqueResult();

			
			if (itemToDelete != null) {
				session.remove(itemToDelete);
				transaction.commit();
				return true; 
			} else {
				return false; 
			}
	    } catch (Exception e) {
	        if (transaction != null) {
	            transaction.rollback();
	        }
	        return false;
	    } finally {
	        if (session != null && session.isOpen()) {
	            session.close();
	        }
	    }
	}
}