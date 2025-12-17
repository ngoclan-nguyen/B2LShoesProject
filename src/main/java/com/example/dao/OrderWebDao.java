package com.example.dao;

import com.example.config.HibernateUtil;
import com.example.model.OrderWeb;
import com.example.model.OrderWebDetail;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class OrderWebDao {

    @Autowired
    private SessionFactory sessionFactory;

    @PersistenceContext
    private EntityManager entityManager;

    public void saveOrder(OrderWeb order) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            // Sử dụng save() thay vì merge() cho đơn hàng MỚI để lấy ID chính xác hơn
            // Hoặc giữ merge() nhưng PHẢI gán lại đối tượng trả về
            OrderWeb mergedOrder = (OrderWeb) session.merge(order);

            // Ép Hibernate đẩy dữ liệu xuống DB ngay lập tức
            session.flush();

            // Cập nhật lại ID cho đối tượng gốc để Controller có thể lấy được
            order.setId(mergedOrder.getId());

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }

    public void saveDetail(OrderWebDetail detail) {
        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            // Thực hiện thao tác
            session.save(detail);

            // Commit Transaction
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            // Ném lại ngoại lệ để Service Layer xử lý
            throw new RuntimeException("Lỗi khi lưu Order Detail: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public List<OrderWeb> findByCustomerId(Long customerId) {
        Session session = null;
        Transaction transaction = null;
        List<OrderWeb> orders = Collections.emptyList();

        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            String hql = "FROM OrderWeb o WHERE o.customer.id = :customerId ORDER BY o.createdAt DESC";

            Query<OrderWeb> query = session.createQuery(hql, OrderWeb.class);
            query.setParameter("customerId", customerId);

            orders = query.list();

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }

        return orders;
    }
    public OrderWeb findByIdAndCustomerId(Long orderId, Long customerId) {
        Session session = null;
        Transaction transaction = null;
        OrderWeb order = null;

        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            String hql = "FROM OrderWeb ow " +
                    "LEFT JOIN FETCH ow.orderDetails od " +
                    "LEFT JOIN FETCH od.productVariant pv " +
                    "WHERE ow.id = :orderId AND ow.customer.id = :customerId";

            Query<OrderWeb> query = session.createQuery(hql, OrderWeb.class);
            query.setParameter("orderId", orderId);
            query.setParameter("customerId", customerId);

            order = query.uniqueResult();

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }

        return order;
    }
    public OrderWeb findOrderById(Long orderId) {
        Session session = HibernateUtil.getSession();
        Transaction transaction = null;
        OrderWeb order = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            // JOIN FETCH giúp lấy Order -> Details -> Variant -> Product -> Image
            String hql = "SELECT DISTINCT o FROM OrderWeb o " +
                    "LEFT JOIN FETCH o.orderDetails od " +
                    "LEFT JOIN FETCH od.productVariant pv " +
                    "LEFT JOIN FETCH pv.product p " +
                    "WHERE o.id = :orderId";

            order = session.createQuery(hql, OrderWeb.class)
                    .setParameter("orderId", orderId)
                    .uniqueResult();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
        return order;
    }
}