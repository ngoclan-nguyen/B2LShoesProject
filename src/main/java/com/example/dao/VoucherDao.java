package com.example.dao;

import com.example.config.HibernateUtil;
import com.example.model.Voucher;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Repository
public class VoucherDao {

    // Lấy tất cả Voucher
    public List<Voucher> findAll() {
        Session session = null;
        Transaction transaction = null;
        List<Voucher> vouchers = new ArrayList<>();

        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            String hql = "FROM Voucher v ORDER BY v.id DESC";
            Query<Voucher> query = session.createQuery(hql, Voucher.class);

            vouchers = query.getResultList();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
        return vouchers;
    }

    // Lấy Voucher theo ID
    public Voucher findById(Long id) {
        Session session = null;
        Transaction transaction = null;
        Voucher voucher = null;

        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            voucher = session.get(Voucher.class, id);

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
        return voucher;
    }

    // Thêm mới hoặc Cập nhật
    public void save(Voucher voucher) {
        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            if (voucher.getId() == null) {
                session.persist(voucher); // Thêm mới
            } else {
                session.merge(voucher); // Cập nhật
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    // Xóa Voucher
    public void delete(Long id) {
        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            Voucher voucher = session.get(Voucher.class, id);
            if (voucher != null) {
                session.remove(voucher);
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    public Voucher findByCodeIgnoreCase(String code) {
        Session session = null;
        Transaction transaction = null;
        Voucher voucher = null;

        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            // Sử dụng HQL/JPQL để tìm kiếm theo trường 'code'
            String hql = "FROM Voucher v WHERE v.code = :voucherCode";
            Query<Voucher> query = session.createQuery(hql, Voucher.class);
            query.setParameter("voucherCode", code);

            // Lấy kết quả duy nhất, hoặc null nếu không tìm thấy
            voucher = query.uniqueResult();

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
        return voucher;
    }
}