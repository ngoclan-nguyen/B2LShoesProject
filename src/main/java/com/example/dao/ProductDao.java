package com.example.dao;

import com.example.config.HibernateUtil;
import com.example.dto.ProductCardDTO;
import com.example.dto.ProductDetailDTO;
import com.example.dto.ProductDetailDTO.SizeOption;
import com.example.model.Product;
import com.example.model.ProductImage;
import com.example.model.ProductVariant;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ProductDao {
    @PersistenceContext
    private EntityManager entityManager;

    // Lấy sản phẩm nổi bật
    public List<ProductCardDTO> findFeaturedProducts(int limit) {
        List<ProductCardDTO> list = null;
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            String hql = "SELECT new com.example.dto.ProductCardDTO(" +
                    "p.id, p.name, p.price, img.path, s.name, 'New') " +
                    "FROM Product p " +
                    "LEFT JOIN p.productImages img " +
                    "LEFT JOIN p.sport s " +
                    "WHERE p.isDelete = false " +
                    "AND (img.isPrimary = true OR img.id IS NULL) " +
                    "ORDER BY p.createdAt DESC";

            Query<ProductCardDTO> query = session.createQuery(hql);
            query.setMaxResults(limit);
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

    // Lấy sản phẩm bán chạy
    public List<ProductCardDTO> findBestSellerProducts(int limit) {
        List<ProductCardDTO> list = null;
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            String hql = "SELECT new com.example.dto.ProductCardDTO(" +
                    "p.id, p.name, p.price, img.path, s.name, 'Best Seller') " +
                    "FROM Product p " +
                    "LEFT JOIN p.productImages img " +
                    "LEFT JOIN p.sport s " +
                    "WHERE p.isDelete = false " +
                    "AND p.status = 'Đang bán' " +
                    "AND (img.isPrimary = true OR img.id IS NULL) " +
                    "ORDER BY p.price DESC";

            Query<ProductCardDTO> query = session.createQuery(hql);
            query.setMaxResults(limit);
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

    // Lọc sản phẩm theo giới tính
    public List<ProductCardDTO> findProductsByGender(String gender) {
        List<ProductCardDTO> list = null;
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            String hql = "SELECT new com.example.dto.ProductCardDTO(" +
                    "p.id, p.name, p.price, img.path, s.name, 'New') " +
                    "FROM Product p " +
                    "LEFT JOIN p.productImages img " +
                    "LEFT JOIN p.sport s " +
                    "WHERE p.isDelete = false " +
                    "AND p.status = 'Đang bán' " +
                    "AND (img.isPrimary = true OR img.id IS NULL) ";

            if (gender != null && !gender.isEmpty()) {
                if (gender.equalsIgnoreCase("Nam")) {
                    hql += " AND (p.gender = 'Nam' OR p.gender = 'Unisex') ";
                } else if (gender.equalsIgnoreCase("Nu")) {
                    hql += " AND (p.gender = 'Nữ' OR p.gender = 'Unisex') ";
                }
            }

            hql += " ORDER BY p.createdAt DESC";

            Query<ProductCardDTO> query = session.createQuery(hql);

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

    // Tìm kiếm sản phẩm
    public List<ProductCardDTO> searchProducts(String keyword, String sort, int page, int size) {
        List<ProductCardDTO> list = null;
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            String hql = "SELECT new com.example.dto.ProductCardDTO(" +
                    "p.id, p.name, p.price, img.path, s.name, 'SearchResult') " +
                    "FROM Product p " +
                    "LEFT JOIN p.productImages img " +
                    "LEFT JOIN p.sport s " +
                    "WHERE p.isDelete = false " +
                    "AND p.status = 'Đang bán' " +
                    "AND (img.isPrimary = true OR img.id IS NULL) ";

            if (keyword != null && !keyword.trim().isEmpty()) {
                hql += " AND LOWER(p.name) LIKE :keyword ";
            }

            if ("newest".equals(sort)) {
                hql += " ORDER BY p.createdAt DESC ";
            } else if ("price_asc".equals(sort)) {
                hql += " ORDER BY p.price ASC ";
            } else if ("price_desc".equals(sort)) {
                hql += " ORDER BY p.price DESC ";
            } else {
                hql += " ORDER BY p.name ASC ";
            }

            Query<ProductCardDTO> query = session.createQuery(hql);

            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
            }

            query.setFirstResult(page * size);
            query.setMaxResults(size);

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

    // Đếm tổng số kết quả tìm kiếm
    public long countSearchProducts(String keyword) {
        long count = 0;
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            String hql = "SELECT count(p.id) FROM Product p " +
                    "WHERE p.isDelete = false AND p.status = 'Đang bán' ";

            if (keyword != null && !keyword.trim().isEmpty()) {
                hql += " AND LOWER(p.name) LIKE :keyword ";
            }

            Query<Long> query = session.createQuery(hql, Long.class);

            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
            }

            count = query.uniqueResult();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
        return count;
    }

    // Gợi ý tìm kiếm
    public List<ProductCardDTO> searchSuggestions(String keyword) {
        List<ProductCardDTO> list = null;
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            String hql = "SELECT new com.example.dto.ProductCardDTO(" +
                    "p.id, p.name, p.price, img.path, s.name, 'Suggestion') " +
                    "FROM Product p " +
                    "LEFT JOIN p.productImages img " +
                    "LEFT JOIN p.sport s " +
                    "WHERE p.isDelete = false " +
                    "AND p.status = 'Đang bán' " +
                    "AND (img.isPrimary = true OR img.id IS NULL) " +
                    "AND LOWER(p.name) LIKE :keyword " +
                    "ORDER BY p.name ASC";

            Query<ProductCardDTO> query = session.createQuery(hql);
            query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");

            query.setMaxResults(5);

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

    public ProductDetailDTO findProductDetailById(Long id) {
        ProductDetailDTO product = null;
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            String hql = "SELECT new com.example.dto.ProductDetailDTO(" +
                    "p.id, p.name, p.price, p.description, c.name, b.name) " +
                    "FROM Product p " +
                    "LEFT JOIN p.category c " +
                    "LEFT JOIN p.brand b " +
                    "WHERE p.id = :id AND p.isDelete = false";

            Query<ProductDetailDTO> query = session.createQuery(hql);
            query.setParameter("id", id);

            product = query.uniqueResult();

            if (product != null) {
                String imgHql = "SELECT i.path FROM ProductImage i WHERE i.product.id = :id";
                org.hibernate.query.Query<String> imgQuery = session.createQuery(imgHql);
                imgQuery.setParameter("id", id);
                product.setImages(imgQuery.list());

                String sizeHql = "SELECT ms.sizeName, v.quantity FROM ProductVariant v " +
                        "JOIN v.size ms " +
                        "WHERE v.product.id = :id " +
                        "ORDER BY ms.id ASC";

                org.hibernate.query.Query<Object[]> sizeQuery = session.createQuery(sizeHql, Object[].class);
                sizeQuery.setParameter("id", id);
                List<Object[]> rows = sizeQuery.list();

                // 3. Map dữ liệu vào List<SizeOption>
                List<ProductDetailDTO.SizeOption> sizeList = new java.util.ArrayList<>();
                for (Object[] row : rows) {
                    sizeList.add(new ProductDetailDTO.SizeOption(
                            (String) row[0], // Tên size
                            (Integer) row[1] // Số lượng
                    ));
                }
                product.setSizes(sizeList);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
        return product;
    }

    // Lấy sản phẩm liên quan
    public List<ProductCardDTO> findRelatedProducts(String brandName, Long currentProductId, int limit) {
        List<ProductCardDTO> list = null;
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            String hql = "SELECT new com.example.dto.ProductCardDTO(" +
                    "p.id, p.name, p.price, img.path, s.name, 'Related') " +
                    "FROM Product p " +
                    "LEFT JOIN p.productImages img " +
                    "LEFT JOIN p.sport s " +
                    "LEFT JOIN p.brand b " +
                    "WHERE p.isDelete = false " +
                    "AND p.status = 'Đang bán' " +
                    "AND (img.isPrimary = true OR img.id IS NULL) ";

            if (brandName != null) {
                hql += (" AND b.name = :brandName ");
            }

            if (currentProductId != null) {
                hql += (" AND p.id != :currentId ");
            }

            hql += (" ORDER BY p.createdAt DESC");

            Query<ProductCardDTO> query = session.createQuery(hql); // Removed .class

            if (brandName != null) {
                query.setParameter("brandName", brandName);
            }
            if (currentProductId != null) {
                query.setParameter("currentId", currentProductId);
            }

            query.setMaxResults(limit);

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

    // Tìm variant theo size (Dùng cho Add To Cart)
    public ProductVariant findVariantByProductAndSize(Long productId, String sizeName) {
        try {
            String hql = "SELECT v FROM ProductVariant v " +
                    "JOIN v.size s " +
                    "WHERE v.product.id = :pid AND s.sizeName = :sname";

            // Sử dụng getResultStream().findFirst() để an toàn hơn, tránh lỗi NoResultException
            return entityManager.createQuery(hql, ProductVariant.class)
                    .setParameter("pid", productId)
                    .setParameter("sname", sizeName)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Tìm variant đầu tiên (Dùng cho Add To Cart nhanh)
    public ProductVariant findFirstVariantByProductId(Long productId) {
        ProductVariant variant = null;
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            String hql = "FROM ProductVariant v " +
                    "WHERE v.product.id = :pid " +
                    "AND v.quantity > 0 " +
                    "ORDER BY v.id ASC";

            Query<ProductVariant> query = session.createQuery(hql, ProductVariant.class);
            query.setParameter("pid", productId);
            query.setMaxResults(1);

            variant = query.uniqueResult();

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
        return variant;
    }

    public List<ProductCardDTO> filterProducts(
            List<String> gender,
            List<String> sport,
            List<String> brand,
            List<String> category,
            List<String> size,
            List<String> color,
            String priceRange,
            String keyword) {

        List<ProductCardDTO> list = null;
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            String hql = "SELECT new com.example.dto.ProductCardDTO(" +
                    "p.id, p.name, p.price, img.path, s.name, 'Filtered') " +
                    "FROM Product p " +
                    "LEFT JOIN p.productImages img " +
                    "LEFT JOIN p.sport s " +
                    "LEFT JOIN p.brand b " +
                    "LEFT JOIN p.category c " +
                    "LEFT JOIN p.color co " +
                    "LEFT JOIN p.variants v " +
                    "LEFT JOIN v.size ms " +
                    "WHERE p.isDelete = false " +
                    "AND p.status = 'Đang bán' " +
                    "AND (img.isPrimary = true OR img.id IS NULL) ";

            if (gender != null && !gender.isEmpty()) {
                hql += " AND p.gender IN :gender ";
            }

            if (sport != null && !sport.isEmpty()) {
                hql += " AND (";
                for (int i = 0; i < sport.size(); i++) {
                    if (i > 0) hql += " OR ";
                    String sVal = sport.get(i);
                    if (sVal.equalsIgnoreCase("Running")) hql += "s.name LIKE '%Chạy bộ%'";
                    else if (sVal.equalsIgnoreCase("Football")) hql += "s.name LIKE '%Bóng đá%'";
                    else if (sVal.equalsIgnoreCase("Basketball")) hql += "s.name LIKE '%Bóng rổ%'";
                    else if (sVal.equalsIgnoreCase("Gym")) hql += "s.name LIKE '%GYM%'";
                    else if (sVal.equalsIgnoreCase("Volleyball")) hql += "s.name LIKE '%Bóng chuyền%'";
                    else if (sVal.equalsIgnoreCase("Badminton")) hql += "s.name LIKE '%Cầu lông%'";
                    else hql += "s.name LIKE '%" + sVal + "%'";
                }
                hql += ") ";
            }

            if (brand != null && !brand.isEmpty()) {
                hql += " AND b.name IN :brand ";
            }

            if (category != null && !category.isEmpty()) {
                hql += " AND c.name IN :category ";
            }

            if (color != null && !color.isEmpty()) {
                hql += " AND co.name IN :color ";
            }

            if (size != null && !size.isEmpty()) {
                hql += " AND (";
                for (int i = 0; i < size.size(); i++) {
                    if (i > 0) hql += " OR ";
                    hql += " ms.sizeName LIKE '" + size.get(i) + "%' ";
                }
                hql += ") ";
            }

            if (priceRange != null && !priceRange.isEmpty()) {
                if (priceRange.equals("lt1m")) {
                    hql += " AND p.price < 1000000 ";
                } else if (priceRange.equals("1m-2m")) {
                    hql += " AND p.price BETWEEN 1000000 AND 2000000 ";
                } else if (priceRange.equals("gt2m")) {
                    hql += " AND p.price > 2000000 ";
                }
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                hql += " AND lower(p.name) LIKE :keyword ";
            }

            hql += " GROUP BY p.id, p.name, p.price, img.path, s.name, p.createdAt ";

            hql += " ORDER BY p.createdAt DESC ";

            org.hibernate.query.Query<ProductCardDTO> query = session.createQuery(hql);

            if (gender != null && !gender.isEmpty()) query.setParameterList("gender", gender);
            if (brand != null && !brand.isEmpty()) query.setParameterList("brand", brand);
            if (category != null && !category.isEmpty()) query.setParameterList("category", category);
            if (color != null && !color.isEmpty()) query.setParameterList("color", color);

            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword.trim().toLowerCase() + "%");
            }
            list = query.list();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
        return list;
    }

    public Product findById(Long id) {
        Product product = null;
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            product = session.get(Product.class, id);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
        return product;
    }

    public List<Product> findAll() {
        Session session = null;
        Transaction transaction = null;

        List<Product> products = new ArrayList<>();

        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            String hql = "SELECT DISTINCT p FROM Product p " +
                    "LEFT JOIN FETCH p.productImages " +
                    "WHERE (p.isDelete = false OR p.isDelete IS NULL) " +
                    "ORDER BY p.id DESC";

            products = session.createQuery(hql, Product.class).getResultList();

            for (Product p : products) {
                List<ProductImage> images = p.getProductImages();
                if (images != null && !images.isEmpty()) {
                    String primaryPath = null;
                    for (ProductImage img : images) {
                        if (Boolean.TRUE.equals(img.getIsPrimary())) {
                            primaryPath = img.getPath();
                            break;
                        }
                    }
                    if (primaryPath == null) {
                        primaryPath = images.get(0).getPath();
                    }
                    p.setImage(primaryPath);
                }
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }

        return products;
    }
}