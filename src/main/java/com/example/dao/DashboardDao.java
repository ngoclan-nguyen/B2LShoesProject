package com.example.dao;

import com.example.model.OrderWeb;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class DashboardDao {

    @PersistenceContext
    private EntityManager entityManager;

    public Long getTotalRevenue() {
        try {
            // Lưu ý: Tên class là OrderWeb, thuộc tính là totalAmount
            String hql = "SELECT SUM(o.totalAmount) FROM OrderWeb o WHERE o.deliveryStatus = 'Hoàn thành'";
            Query query = entityManager.createQuery(hql);
            Object result = query.getSingleResult();
            return (result != null) ? (Long) result : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    public Long getNewOrdersCount() {
        try {
            String hql = "SELECT COUNT(o) FROM OrderWeb o WHERE o.deliveryStatus = 'Chờ xác nhận'";
            Query query = entityManager.createQuery(hql);
            return (Long) query.getSingleResult();
        } catch (Exception e) {
            return 0L;
        }
    }

    public Long getTotalCustomers() {
        try {
            // Giả sử trong DB bạn lưu role khách hàng là 'CUSTOMER'
            String hql = "SELECT COUNT(u) FROM User u WHERE u.role = 'CUSTOMER'";
            Query query = entityManager.createQuery(hql);
            return (Long) query.getSingleResult();
        } catch (Exception e) {
            return 0L;
        }
    }

    public Long getTotalProducts() {
        try {
            // Chỉ đếm sản phẩm chưa bị xóa
            String hql = "SELECT COUNT(p) FROM Product p WHERE p.isDelete = false";
            Query query = entityManager.createQuery(hql);
            return (Long) query.getSingleResult();
        } catch (Exception e) {
            return 0L;
        }
    }

    public List<OrderWeb> getRecentOrders() {
        String hql = "SELECT o FROM OrderWeb o ORDER BY o.createdAt DESC";
        return entityManager.createQuery(hql, OrderWeb.class)
                .setMaxResults(5) // Limit 5
                .getResultList();
    }
}