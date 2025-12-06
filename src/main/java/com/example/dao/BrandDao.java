package com.example.dao;

import com.example.config.HibernateUtil;
import com.example.dto.BrandDTO;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class BrandDao {
    public List<BrandDTO> findAll() {
        List<BrandDTO> list = null;
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            String hql = "SELECT new com.example.dto.BrandDTO(s.id, s.name) FROM Sport s";

            Query<BrandDTO> query = session.createQuery(hql);

            list = query.list();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
        return list;
    }
}
