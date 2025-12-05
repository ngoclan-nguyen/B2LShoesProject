package com.example.dao;

import com.example.config.HibernateUtil;
import com.example.dto.UserDTO;
import com.example.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UserDao {
    /**
     * Hàm lấy tham chiếu của User, không chạy câu lệnh select xuống database
     * Nó chỉ tạo ra một vỏ bọc chứa ID để gán vào khóa ngoại
     */
    public User getReference(Session session, Long id) {
        // Hàm này tạo ra một vỏ bọc User chỉ chứa ID, không tốn query SELECT
        return session.getReference(User.class, id);
    }
}