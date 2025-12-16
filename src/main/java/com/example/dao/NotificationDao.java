package com.example.dao;

import com.example.config.HibernateUtil;
import com.example.model.Notification;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class NotificationDao {

    //  Lấy số lượng thông báo chưa đọc
    public Integer getUnreadCount() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(n) FROM Notification n WHERE n.isRead = false";
            Long count = session.createQuery(hql, Long.class).uniqueResult();
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            System.err.println("Lỗi DAO khi lấy số lượng thông báo chưa đọc: " + e.getMessage());
            return 0;
        }
    }

    // Lấy N thông báo gần đây nhất (Ưu tiên chưa đọc)
    public List<Notification> getRecentNotifications(int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Sắp xếp theo: Chưa đọc trước, sau đó theo thời gian tạo mới nhất
            String hql = "FROM Notification n ORDER BY n.isRead ASC, n.createdAt DESC";

            return session.createQuery(hql, Notification.class)
                    .setMaxResults(limit)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Lỗi DAO khi lấy thông báo gần đây: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // Đánh dấu thông báo là đã đọc (Theo ID)
    public boolean markAsRead(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();

            String hql = "UPDATE Notification n SET n.isRead = true WHERE n.id = :id";
            session.createQuery(hql)
                    .setParameter("id", id)
                    .executeUpdate();

            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("Lỗi DAO khi đánh dấu thông báo đã đọc: " + e.getMessage());
            return false;
        } finally {
            session.close();
        }
    }

    //  Lưu một thông báo mới
    public void save(Notification notification) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(notification);
            transaction.commit();
        } catch (Exception e) {
            System.err.println("Lỗi DAO khi lưu thông báo: " + e.getMessage());
        }
    }

    public List<Notification> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Notification n ORDER BY n.createdAt DESC";
            return session.createQuery(hql, Notification.class).getResultList();
        } catch (Exception e) {
            System.err.println("Lỗi DAO khi lấy tất cả thông báo: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}