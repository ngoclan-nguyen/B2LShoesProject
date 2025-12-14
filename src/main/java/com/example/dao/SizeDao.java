package com.example.dao;

import com.example.config.HibernateUtil;
import com.example.dto.SportDTO;
import com.example.model.MasterSize;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
@Repository
public class SizeDao {
    public List<String> getAllSizeNames() {
        List<String> sizeNames = new ArrayList<>();
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            String hql = "FROM MasterSize s WHERE s.type = 'SHOES' ORDER BY s.id ASC";

            Query<MasterSize> query = session.createQuery(hql, MasterSize.class);
            List<MasterSize> masterSizes = query.list();

            for (MasterSize ms : masterSizes) {
                String name = ms.getSizeName();

                if (!sizeNames.contains(name)) {
                    sizeNames.add(name);
                }
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
        return sizeNames;
    }
}
