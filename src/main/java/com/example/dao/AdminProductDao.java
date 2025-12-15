package com.example.dao;

import com.example.config.HibernateUtil;
import com.example.model.Brand;
import com.example.model.Category;
import com.example.model.Product;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AdminProductDao {

    public List<Product> searchProducts(String keyword, Long brandId, Long categoryId) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            // Khởi tạo câu HQL cơ bản
            StringBuilder hql = new StringBuilder("SELECT p FROM Product p WHERE p.isDelete = false");

            // Nối chuỗi HQL nếu có điều kiện
            if (keyword != null && !keyword.trim().isEmpty()) {
                hql.append(" AND p.name LIKE :keyword");
            }
            if (brandId != null) {
                hql.append(" AND p.brand.id = :brandId");
            }
            if (categoryId != null) {
                hql.append(" AND p.category.id = :categoryId");
            }

            hql.append(" ORDER BY p.createdAt DESC");

            // Tạo Query
            Query<Product> query = session.createQuery(hql.toString(), Product.class);

            // Set tham số vào Query
            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword + "%");
            }
            if (brandId != null) {
                query.setParameter("brandId", brandId);
            }
            if (categoryId != null) {
                query.setParameter("categoryId", categoryId);
            }

            List<Product> products = query.getResultList();

            for (Product p : products) {
                String imgPath = getPrimaryImageInternal(session, p.getId());
                p.setImage(imgPath);
            }

            transaction.commit();
            return products;

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    public List<Brand> getAllBrands() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Brand", Brand.class).list();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<Category> getAllCategories() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Category", Category.class).list();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    public Product getProductById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Product p = session.get(Product.class, id);

            if (p != null) {
                Hibernate.initialize(p.getProductImages());
            }
            return p;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean saveOrUpdate(Product product) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();

            // Nếu là thêm mới, set thời gian tạo
            if (product.getId() == null) {
                product.setCreatedAt(LocalDateTime.now());
                product.setIsDelete(false);
            }
            product.setUpdatedAt(LocalDateTime.now());

            // saveOrUpdate: nếu id null -> Insert, nếu id có -> Update
            session.merge(product);

            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    private String getPrimaryImageInternal(Session session, Long productId) {
        try {
            // Lấy ảnh có isPrimary = true
            String hql = "SELECT pi.path FROM ProductImage pi WHERE pi.product.id = :pid AND pi.isPrimary = true";
            List<String> results = session.createQuery(hql, String.class)
                    .setParameter("pid", productId)
                    .setMaxResults(1)
                    .getResultList();

            if (!results.isEmpty()) {
                return results.get(0);
            }

            // Nếu không có ảnh primary, lấy ảnh bất kỳ
            String hqlFallback = "SELECT pi.path FROM ProductImage pi WHERE pi.product.id = :pid";
            List<String> fallback = session.createQuery(hqlFallback, String.class)
                    .setParameter("pid", productId)
                    .setMaxResults(1)
                    .getResultList();

            return fallback.isEmpty() ? "/admin/images/no-image.png" : fallback.get(0);
        } catch (Exception e) {
            return "/admin/images/no-image.png";
        }
    }

    public List<Product> getAllProducts() {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            String hql = "SELECT p FROM Product p WHERE p.isDelete = false ORDER BY p.createdAt DESC";
            List<Product> products = session.createQuery(hql, Product.class).getResultList();

            // Điền link ảnh đại diện
            for (Product p : products) {
                String imgPath = getPrimaryImageInternal(session, p.getId());
                p.setImage(imgPath);
            }

            transaction.commit();
            return products;

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public boolean deleteProduct(Long id) {
        Session session = HibernateUtil.getSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();

            String hql = "UPDATE Product p SET p.isDelete = true WHERE p.id = :id";
            Query<?> query = session.createQuery(hql);
            query.setParameter("id", id);

            int rowCount = query.executeUpdate();

            transaction.commit();

            System.out.println("Đã xóa sản phẩm ID: " + id + ". Số dòng ảnh hưởng: " + rowCount);
            return rowCount > 0;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }
}