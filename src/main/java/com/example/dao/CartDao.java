package com.example.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.example.config.HibernateUtil;
import com.example.model.CartItem;
import com.example.model.ProductVariant;
import com.example.model.User;


@Repository
public class CartDao {
    private static final Logger logger = LoggerFactory.getLogger(CartDao.class);
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
            logger.error("Error finding cart item by product and user", e);
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

            session.merge(cartItem);

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("Error saving cart item", e);
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
            logger.error("Error counting cart items by user", e);
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
            ProductVariant variant;
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
                existingItem.setProductVariant(variant);
                session.merge(existingItem);
            } else {
                // Chưa có -> Tạo mới
                CartItem newItem = new CartItem();
                newItem.setUser(userRef);         // Gán User Proxy
                newItem.setProductVariant(variant); // Gán Sản phẩm
                newItem.setQuantity(quantity);
                session.merge(newItem);
            }

            // 5. Đếm lại tổng số lượng để cập nhật icon
            totalItems = countItemsByUser(userId);

            // 6. Lưu tất cả thay đổi xuống DB
            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("Error adding item to cart", e);
            return -4; // Lỗi hệ thống
        } finally {
            if (session != null) session.close();
        }

        return totalItems;
    }
    public List<CartItem> findItemsByUser(Long userId) {
    Session session = null;
    Transaction tx = null;
    try {
        session = HibernateUtil.getSession();
        tx = session.beginTransaction();

        String hql = """
            select distinct c
            from CartItem c
            join fetch c.productVariant pv
            join fetch pv.product p
            join fetch pv.size s
            left join fetch p.productImages imgs
            where c.user.id = :uid
        """;

        List<CartItem> items = session.createQuery(hql, CartItem.class)
                .setParameter("uid", userId)
                .getResultList();

        tx.commit();
        return items;
    } catch (Exception e) {
        if (tx != null) tx.rollback();
        throw new RuntimeException("Load cart failed", e);
    } finally {
        if (session != null) session.close();
    }
}

public void deleteItemsByUser(Long userId) {
    Session session = null;
    Transaction tx = null;
    try {
        session = HibernateUtil.getSession();
        tx = session.beginTransaction();

        session.createQuery("delete from CartItem c where c.user.id = :uid", CartItem.class)
                .setParameter("uid", userId)
                .executeUpdate();

        tx.commit();
    } catch (Exception e) {
        if (tx != null) tx.rollback();
        throw new RuntimeException("Clear cart failed", e);
    } finally {
        if (session != null) session.close();
    }
}
}
