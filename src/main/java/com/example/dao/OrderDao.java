package com.example.dao;

import com.example.model.OrderWeb;
import com.example.config.HibernateUtil;
import com.example.model.OrderWebDetail;
import com.example.model.Product;
import com.example.model.ProductImage;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OrderDao {

    // Lấy danh sách đơn hàng (Có lọc theo trạng thái)
    public List<OrderWeb> findAll(String status) {
        Session session = null;
        Transaction transaction = null;
        List<OrderWeb> orders = new ArrayList<>();

        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            StringBuilder hql = new StringBuilder("SELECT DISTINCT o FROM OrderWeb o ");
            hql.append("LEFT JOIN FETCH o.customer c "); // Tải thông tin khách hàng
            hql.append("LEFT JOIN FETCH o.orderDetails od "); // Tải chi tiết đơn hàng

            // Xử lý Lọc theo Trạng thái
            if (status != null && !status.equalsIgnoreCase("Tất cả")) {
                // Đảm bảo tên cột status là deliveryStatus (hoặc tên cột bạn dùng)
                hql.append("WHERE o.deliveryStatus = :status ");
            }

            hql.append("ORDER BY o.id DESC");

            Query<OrderWeb> query = session.createQuery(hql.toString(), OrderWeb.class);

            if (status != null && !status.equalsIgnoreCase("Tất cả")) {
                query.setParameter("status", status);
            }

            orders = query.getResultList();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
        return orders;
    }

    // Lấy chi tiết đơn hàng theo ID
    public OrderWeb findById(Long id) {
        Session session = null;
        Transaction transaction = null;
        OrderWeb order = null;

        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            String hql = "SELECT o FROM OrderWeb o " +
                    "LEFT JOIN FETCH o.customer c " +
                    "LEFT JOIN FETCH o.orderDetails od " + // Vẫn giữ FETCH cho OrderDetails
                    "LEFT JOIN FETCH od.productVariant pv " +
                    "LEFT JOIN FETCH pv.product p " +
                    "LEFT JOIN FETCH pv.size ms " +
                    "LEFT JOIN FETCH p.color cl " +
                    // "LEFT JOIN FETCH p.productImages pi " + // <--- BỎ DÒNG NÀY (Không JOIN FETCH ProductImages)
                    "WHERE o.id = :id";

            order = session.createQuery(hql, OrderWeb.class)
                    .setParameter("id", id)
                    .uniqueResult();
            if (order != null && order.getOrderDetails() != null) {
                for (OrderWebDetail detail : order.getOrderDetails()) {
                    if (detail.getProductVariant() != null && detail.getProductVariant().getProduct() != null) {
                        Product product = detail.getProductVariant().getProduct();

                        // Lấy danh sách ảnh đã được tải (do Lazy loading hoặc Fetch)
                        List<ProductImage> images = product.getProductImages();

                        // Logic tìm ảnh chính
                        String primaryPath = images.stream()
                                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                                .findFirst()
                                .map(ProductImage::getPath)
                                .orElse(images.isEmpty() ? "/path/to/default/image.png" : images.get(0).getPath());

                        // GÁN VÀO TRƯỜNG @Transient 'image'
                        product.setImage(primaryPath);
                    }
                }
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
        return order;
    }

    // Cập nhật trạng thái đơn hàng
    public void updateDeliveryStatus(Long id, String newStatus) {
        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            // Sử dụng HQL UPDATE trực tiếp
            String hql = "UPDATE OrderWeb o SET o.deliveryStatus = :newStatus WHERE o.id = :orderId";

            session.createQuery(hql)
                    .setParameter("newStatus", newStatus)
                    .setParameter("orderId", id)
                    .executeUpdate();

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Cập nhật trạng thái thất bại: " + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    public List<OrderWeb> findFilteredOrders(String status, String keyword) {

        Session session = null;
        Transaction transaction = null;
        List<OrderWeb> orders = new ArrayList<>();

        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction(); // Bắt đầu Transaction ĐỌC

            // --- 1. Xây dựng Truy vấn HQL Động ---
            StringBuilder queryBuilder = new StringBuilder("SELECT o FROM OrderWeb o WHERE 1=1");
            Map<String, Object> params = new HashMap<>();

            // 2. Lọc theo Trạng thái
            if (status != null && !status.isEmpty() && !"Tất cả".equals(status)) {
                queryBuilder.append(" AND o.deliveryStatus = :status");
                params.put("status", status);
            }

            // 3. Tìm kiếm theo Từ khóa
            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeKeyword = "%" + keyword.trim() + "%";
                queryBuilder.append(" AND (");
                queryBuilder.append(" o.consignee LIKE :keyword ");
                queryBuilder.append(" OR o.phoneNumber LIKE :keyword ");

                try {
                    Long orderId = Long.parseLong(keyword.trim());
                    queryBuilder.append(" OR o.id = :orderIdExact ");
                    params.put("orderIdExact", orderId);
                } catch (NumberFormatException ignored) {
                    // Nếu keyword không phải số, bỏ qua tìm kiếm theo ID
                }

                params.put("keyword", likeKeyword);
                queryBuilder.append(")");
            }

            // LOGIC LỌC THEO DATE RANGE ĐÃ ĐƯỢC DỌN DẸP HOÀN TOÀN

            queryBuilder.append(" ORDER BY o.createdAt DESC");
            String finalQuery = queryBuilder.toString();
            // --- Kết thúc Xây dựng Truy vấn ---

            // 4. Thực thi Truy vấn
            Query<OrderWeb> query = session.createQuery(finalQuery, OrderWeb.class);

            // 5. Gán các tham số
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                // Chỉ còn xử lý kiểu Long và kiểu chung (String)
                if (entry.getValue() instanceof Long) {
                    query.setParameter(entry.getKey(), (Long) entry.getValue());
                } else {
                    query.setParameter(entry.getKey(), entry.getValue());
                }
            }

            orders = query.getResultList();
            transaction.commit(); // Commit Transaction ĐỌC thành công

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback(); // Rollback nếu có lỗi
            }
            System.err.println("Lỗi khi thực thi truy vấn lọc đơn hàng: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return orders;
    }

    public List<OrderWeb> findAll() {
        return findFilteredOrders("Tất cả", null);
    }
}