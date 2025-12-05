package com.example.dao;

import com.example.config.HibernateUtil;
import com.example.dto.ProductCardDTO;
import com.example.dto.ProductDetailDTO;


import com.example.model.ProductVariant;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@Repository	
@Transactional(readOnly = true) // Chỉ đọc dữ liệu, giúp tối ưu hiệu năng
public class ProductDao {


    /**
     * Lấy sản phẩm chưa xóa, sắp xếp ngày tạo mới nhất
     */
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
         }
         catch (Exception e) {
             if (transaction != null) transaction.rollback();
             e.printStackTrace();
         }
         finally {
             if (session != null) session.close();
         }

         return list;
     }

    /**
     * Lấy tạm sản phẩm có status 'Đang bán', sắp xếp giá cao
     */
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
	     }
	     catch (Exception e) {
	         if (transaction != null) transaction.rollback();
	         e.printStackTrace();
	     }
	     finally {
	         if (session != null) session.close();
	     }
	
	     return list;
 }

    
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

            Query<ProductCardDTO> query = session.createQuery(hql, ProductCardDTO.class);

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
                // Mặc định sắp xếp theo tên
                hql += " ORDER BY p.name ASC ";
            }

            org.hibernate.query.Query<ProductCardDTO> query = session.createQuery(hql);

            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
            }

            // Phân trang
            query.setFirstResult(page * size); // Vị trí bắt đầu
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

    // Đếm tổng số kết quả tìm kiểm để tính số trang
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

            Query<Long> query = session.createQuery(hql);

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

    @PersistenceContext
    private EntityManager entityManager;

    public ProductDetailDTO findProductDetailById(Long id) {
        Session session = entityManager.unwrap(Session.class);
        try {
            String hql = "SELECT new com.example.dto.ProductDetailDTO(" +
                    "p.id, p.name, p.price, p.description, s.name, b.name) " +
                    "FROM Product p " +
                    "LEFT JOIN p.sport s " +
                    "LEFT JOIN p.brand b " +
                    "WHERE p.id = :id AND p.isDelete = false";

            Query<ProductDetailDTO> query = session.createQuery(hql);
            query.setParameter("id", id);

            ProductDetailDTO product = query.uniqueResult();

            // Nếu tìm thấy sản phẩm thì lấy thêm Ảnh và Size
            if (product != null) {
                // Lấy ảnh
                String imgHql = "SELECT i.path FROM ProductImage i WHERE i.product.id = :id";
                org.hibernate.query.Query<String> imgQuery = session.createQuery(imgHql);
                imgQuery.setParameter("id", id);
                product.setImages(imgQuery.list());

                // Lấy size
                String sizeHql = "SELECT ms.sizeName FROM ProductVariant v " +
                        "JOIN v.size ms " +
                        "WHERE v.product.id = :id AND v.quantity > 0 " +
                        "ORDER BY ms.id ASC";
                org.hibernate.query.Query<String> sizeQuery = session.createQuery(sizeHql);
                sizeQuery.setParameter("id", id);
                product.setSizes(sizeQuery.list());
            }

            return product;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

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

            // lấy sản phẩm cùng brand nhưng trừ chính sản phẩm đang hiển thị
            if (currentProductId != null) {
                hql += (" AND p.id != :currentId ");
            }

            // Sắp xếp theo thứu tự mới nhất
            hql += (" ORDER BY p.createdAt DESC");

            Query<ProductCardDTO> query = session.createQuery(hql.toString());

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

    public ProductVariant findVariantByProductAndSize(Long productId, String sizeName) {
        ProductVariant variant = null;
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            String hql = "SELECT v FROM ProductVariant v " +
                    "JOIN v.size s " +
                    "WHERE v.product.id = :pid AND s.sizeName = :sname";

            variant = session.createQuery(hql, ProductVariant.class)
                    .setParameter("pid", productId)
                    .setParameter("sname", sizeName)
                    .uniqueResult();

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
        return variant;
    }

    //Thêm vào giỏ hàng ở trang chủ
    public ProductVariant findFirstVariantByProductId(Long productId) {
        ProductVariant variant = null;
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            // Lấy Variant thuộc Product đó và có số lượng > 0
            // Sắp xếp theo ID hoặc Size để lấy cái nhỏ nhất làm mặc định
            String hql = "FROM ProductVariant v " +
                    "WHERE v.product.id = :pid " +
                    "AND v.quantity > 0 " +
                    "ORDER BY v.id ASC";

            Query<com.example.model.ProductVariant> query = session.createQuery(hql);

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
}