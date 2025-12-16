package com.example.dao;

import com.example.model.ProductVariant;
import com.example.config.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;

@Repository
public class ProductVariantDao {
    // Hàm tìm kiếm theo ID (Dùng để lấy thông tin sản phẩm trước khi tạo đơn)
    public ProductVariant findById(Long id) {
        Session session = null;
        Transaction transaction = null;
        ProductVariant variant = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            // Sử dụng session.get để lấy object theo Primary Key
            variant = session.get(ProductVariant.class, id);

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
        return variant;
    }

    // Hàm cập nhật (Dùng để trừ số lượng tồn kho)
    public void update(ProductVariant variant) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            session.merge(variant);

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }
}