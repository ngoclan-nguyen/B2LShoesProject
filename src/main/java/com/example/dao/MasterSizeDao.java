package com.example.dao;

import com.example.config.HibernateUtil;
import com.example.model.MasterSize;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class MasterSizeDao {
    public List<MasterSize> getAllSize() {
        List<MasterSize> list = null;
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            String hql = "FROM MasterSize ms WHERE type = 'SHOES'";

            Query<MasterSize> query = session.createQuery(hql, MasterSize.class);

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
