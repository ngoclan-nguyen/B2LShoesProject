package com.example.dao;

import com.example.model.PaymentLog;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PaymentLogDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional // Đảm bảo việc ghi dữ liệu nằm trong một giao dịch
    public void save(PaymentLog paymentLog) {
        if (paymentLog.getId() == null) {
            // Sử dụng persist() cho Entity mới
            entityManager.persist(paymentLog);
        } else {
            // Sử dụng merge() cho Entity đã tồn tại
            entityManager.merge(paymentLog);
        }
    }

    public PaymentLog findById(Long id) {
        return entityManager.find(PaymentLog.class, id);
    }
}