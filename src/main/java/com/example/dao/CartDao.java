package com.example.dao;

import com.example.model.CartItem;
import com.example.config.HibernateUtil;
import com.example.model.ProductVariant;
import com.example.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CartDao {
    @Autowired
    private ProductDao productDao;
    @Autowired
    private UserDao userDao;

    // Tìm cart item theo sản phẩm và user
    public CartItem findByProductAndUser(Long productVariantId, Long userId) {
        CartItem item = null;
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            String hql = "FROM CartItem c WHERE c.productVariant.id = :vid AND c.user.id = :uid";

            Query<CartItem> query = session.createQuery(hql, CartItem.class);
            query.setParameter("vid", productVariantId);
            query.setParameter("uid", userId);

            item = query.uniqueResult();

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
        return item;
    }

    // Lưu và cập nhật giỏ hàng
    public void save(CartItem cartItem) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            session.saveOrUpdate(cartItem);

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
    }

    // Đếm số lượng sản phẩm của user
    public int countItemsByUser(Long userId) {
        int totalCount = 0;
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            // Tính tổng số lượng (quantity)
            String hql = "SELECT SUM(c.quantity) FROM CartItem c WHERE c.user.id = :uid";

            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("uid", userId);

            Long result = query.uniqueResult();

            // Nếu kết quả không null (tức là có sản phẩm), lấy giá trị int
            if (result != null) {
                totalCount = result.intValue();
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
        return totalCount;
    }

    public int addToCart(Long productId, int quantity, String sizeName, Long userId) {
        Session session = null;
        Transaction transaction = null;
        int totalItems = 0;

        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            // 2. Tìm Variant (Gọi ProductDao, truyền session vào)
            ProductVariant variant = null;
            if (sizeName != null && !sizeName.isEmpty()) {
                variant = productDao.findVariantByProductAndSize(productId, sizeName);
            } else {
                variant = productDao.findFirstVariantByProductId(productId);
            }

            if (variant == null) return -2; // Hết hàng/Không tìm thấy
            if (variant.getQuantity() < quantity) return -3; // Không đủ số lượng

            // 3. Lấy tham chiếu User (Gọi UserDao, truyền session vào)
            // Hàm này trả về Proxy chỉ chứa ID, không tốn query SELECT
            User userRef = userDao.getReference(session, userId);

            // 4. Xử lý Giỏ hàng (Gọi CartDao, truyền session vào)
            CartItem existingItem = findByProductAndUser((long) variant.getId(), userId);

            if (existingItem != null) {
                // Đã có -> Cộng dồn số lượng
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
                save(existingItem);
            } else {
                // Chưa có -> Tạo mới
                CartItem newItem = new CartItem();
                newItem.setUser(userRef);         // Gán User Proxy
                newItem.setProductVariant(variant); // Gán Sản phẩm
                newItem.setQuantity(quantity);
                save(newItem);
            }

            // 5. Đếm lại tổng số lượng để cập nhật icon
            totalItems = countItemsByUser(userId);

            // 6. Lưu tất cả thay đổi xuống DB
            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return -4; // Lỗi hệ thống
        } finally {
            if (session != null) session.close();
        }

        return totalItems;
    }
}
