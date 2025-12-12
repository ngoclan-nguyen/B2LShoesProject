package com.example.dao;

import com.example.config.HibernateUtil;
import com.example.model.ProductReview;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList; // Nhớ import cái này
import java.util.List;

@Repository
public class ProductReviewDao {

    public boolean save(ProductReview review) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            session.persist(review);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        } finally {
            if (session != null) session.close();
        }
    }

    public List<ProductReview> getReviewsByProductId(Long productId) {
        List<ProductReview> reviews = new ArrayList<>(); // Khởi tạo rỗng để tránh null
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            String hql = "SELECT DISTINCT r FROM ProductReview r " +
                    "LEFT JOIN FETCH r.images " +
                    "WHERE r.product.id = :pid ORDER BY r.createdAt DESC";
            Query<ProductReview> query = session.createQuery(hql, ProductReview.class);
            query.setParameter("pid", productId);

            reviews = query.list();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
        return reviews;
    }

    public long countReviews(Long productId) {
        long count = 0;
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            String hql = "SELECT COUNT(r) FROM ProductReview r WHERE r.product.id = :pid";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("pid", productId);

            Long result = query.uniqueResult();
            if (result != null) {
                count = result;
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
        return count;
    }
}