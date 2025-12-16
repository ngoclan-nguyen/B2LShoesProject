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
    public User findByEmail(String email) {
        Session session = null;
        Transaction transaction = null;
        User user = null;

        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            Query<User> query = session.createQuery("FROM User u WHERE u.email = :email", User.class);
            query.setParameter("email", email);
            user = query.uniqueResult();

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
        return user;
    }

    public List<UserDTO> getAllCustomer() {
    	Session session = null;
    	Transaction transaction = null;
    	List<UserDTO> customers = null;
    	
    	try {
    		session = HibernateUtil.getSession();
    		transaction = session.beginTransaction();
    		
    		String hql = "SELECT new com.example.dto.UserDTO(u.id, u.name, u.email, u.phone, u.address, u.gender, u.role, u.title) "
    				+ "FROM User u "
    				+ "WHERE u.role = 'Customer' "
    				+ "AND u.isDeleted = false";
    		Query<UserDTO> query = session.createQuery(hql, UserDTO.class);
    		
    		customers = query.list();
    		transaction.commit();
    	}
    	catch (Exception e) {
			if (transaction != null) transaction.rollback();
		 	e.printStackTrace();
		} finally {
			if (session != null) session.close();
		}
		return customers;
	}	
    
    public UserDTO getUserById(Long userId) {
    	Session session = null;
    	Transaction transaction = null;
    	UserDTO customer = null;
    	
    	try {
    		session = HibernateUtil.getSession();
    		transaction = session.beginTransaction();
    		
    		String hql = "SELECT new com.example.dto.UserDTO(u.id, u.name, u.email, u.phone, u.address, u.gender, u.role, u.title) "
    				+ "FROM User u "
    				+ "WHERE u.role = 'Customer' "
    				+ "AND u.isDeleted = false "
    				+ "AND u.id = :uid";
    		Query<UserDTO> query = session.createQuery(hql, UserDTO.class);
    		query.setParameter("uid", userId);
    		
    		customer = query.uniqueResult();
    		transaction.commit();
    	}
    	catch (Exception e) {
			if (transaction != null) transaction.rollback();
		 	e.printStackTrace();
		} finally {
			if (session != null) session.close();
		}
		return customer;
	}
    
    public List<OrderDetailDTO> getOrderDetailsByOrderId(
            Session session, Long orderId) {

        String hql =
            "SELECT new com.example.dto.OrderDetailDTO( "
            + "od.id, p.id, p.name, p.price, b.name, "
            + "od.totalAmount, od.quantity, s.sizeName, img.path ) "
            + "FROM OrderWebDetail od "
            + "JOIN od.productVariant pv "
            + "JOIN pv.product p "
            + "JOIN p.brand b "
            + "JOIN p.productImages img "
            + "JOIN pv.size s "
            + "WHERE od.orderWeb.id = :oid "
            + "AND img.isPrimary = true";

        return session.createQuery(hql, OrderDetailDTO.class)
                .setParameter("oid", orderId)
                .list();
    }
    
    public List<OrderDTO> getAllOrderByUserId(Long userId) {
        Session session = null;
        Transaction transaction = null;
        List<OrderDTO> orders = new ArrayList<>();

        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            String hql =
                "SELECT new com.example.dto.OrderDTO(" +
                " o.id, o.totalAmount, o.paymentMethod, " +
                " o.paymentStatus, o.deliveryStatus, o.createdAt ) " +
                "FROM OrderWeb o " +
                "WHERE o.customer.id = :uid " +
                "ORDER BY o.createdAt DESC";

            Query<OrderDTO> query = session.createQuery(hql, OrderDTO.class);
            query.setParameter("uid", userId);

            orders = query.list();
            
            for (OrderDTO order : orders) {
                List<OrderDetailDTO> details =
                    getOrderDetailsByOrderId(session, order.getOrderId());
                order.setOrderDetails(details);
            }
            
            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }

        return orders;
    }
    
    public Long deleteUserById(Long userId) {
        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            String updateHql = "UPDATE User u SET u.isDeleted = true WHERE u.id = :uid";
            session.createQuery(updateHql)
                   .setParameter("uid", userId)
                   .executeUpdate();

            String countHql = "SELECT COUNT(u.id) FROM User u WHERE u.isDeleted = false AND u.role='Customer'";
            Long activeUserCount = (Long) session.createQuery(countHql)
                                                 .uniqueResult();

            transaction.commit();

            return activeUserCount;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return 0L;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}
