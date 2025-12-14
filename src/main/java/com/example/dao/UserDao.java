package com.example.dao;

import com.example.config.HibernateUtil;
import com.example.dto.UserDTO;
import com.example.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
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

    public User findUserById(Long id) {
        User user = null;
        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            user = session.get(User.class, id);

            transaction.commit();
        }catch(Exception e) {
            if(transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }finally {
            if(session != null && session.isOpen()) {
                session.close();
            }
        }

        return user;
    }

    public Long checkAccount(String email, String password) {
        Long id = 0L;
        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            Query<User> query1 = session.createQuery("Select u FROM User u WHERE u.email = :email", User.class);
            query1.setParameter("email", email);

            if(query1.list().size() == 0) {
                return -1L;
            }

            Query<User>	query2 = session.createQuery("Select u From User u Where u.email = :email and u.password = :password",User.class);
            query2.setParameter("email", email);
            query2.setParameter("password", password);
            if(query2.list().size() == 0) {
                return -2L;
            }

            id= query2.list().get(0).getId();

            transaction.commit();
        }catch(Exception e) {
            if(transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }finally {
            if(session != null && session.isOpen()) {
                session.close();
            }
        }

        return id;
    }

    public boolean checkEmail(String email) {
        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            Query<User> query = session.createQuery("Select u FROM User u WHERE u.email = :email", User.class);
            query.setParameter("email", email);

            if(query.list().size() == 0) {
                return false;
            }

            transaction.commit();
        }catch(Exception e) {
            if(transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }finally {
            if(session != null && session.isOpen()) {
                session.close();
            }
        }

        return true;
    }

    public boolean checkPhone(String phone) {
        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            Query<User> query = session.createQuery("Select u FROM User u WHERE u.phone = :phone", User.class);
            query.setParameter("phone", phone);

            if(query.list().size() == 0) {
                return false;
            }

            transaction.commit();
        }catch(Exception e) {
            if(transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }finally {
            if(session != null && session.isOpen()) {
                session.close();
            }
        }

        return true;
    }
    public void changePassword(String emailChange, String password) {
        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            Query<User> query = session.createQuery("From User u Where u.email = :email",User.class);
            query.setParameter("email", emailChange);
            User user = query.getSingleResult();
            user.setPassword(password);

            transaction.commit();
        }catch(Exception e) {
            if(transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }finally {
            if(session != null && session.isOpen()) {
                session.close();
            }
        }

    }

    public boolean checkOldPassword(Long userId, String oldPassword) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            User user = session.get(User.class, userId);

            if (user != null && user.getPassword().equals(oldPassword)) {
                return true;
            }
            transaction.commit();
            return false;
        }catch(Exception e) {
            if(transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }finally {
            if(session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public boolean save(User user) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            session.persist(user); // Lưu user mới

            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }


    public boolean updateUser(Long userId, String name, String phone, String address) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            User user = session.get(User.class, userId);
            if (user != null) {
                user.setName(name);
                user.setPhone(phone);
                user.setAddress(address);
                user.setUpdatedAt(java.time.LocalDateTime.now());

                session.merge(user); // Lưu thay đổi
            }

            transaction.commit();
            return true;
        } catch(Exception e) {
            if(transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        } finally {
            if(session != null && session.isOpen()) session.close();
        }
    }
}