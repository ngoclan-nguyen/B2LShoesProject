package com.example.dao;

import com.example.config.HibernateUtil;
import com.example.model.OrderWeb;
import com.example.model.OrderWebDetail;
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

    public Long saveOrder(OrderWeb order) {
        Session session = sessionFactory.getCurrentSession();
        return (Long) session.save(order);
    }

    public void saveDetail(OrderWebDetail detail) {
        Session session = sessionFactory.getCurrentSession();
        session.save(detail);
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
}