package com.example.dao;

import com.example.config.HibernateUtil;
import com.example.dto.ProductCardDTO;


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
     * 1. Lấy danh sách Top Nổi Bật (Mới nhất)
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
     * 2. Lấy danh sách Top Bán Chạy
     * Logic tạm thời: Lấy sản phẩm có status 'Đang bán', sắp xếp giá cao (hoặc logic khác tùy bạn)
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

    // 4. TÌM KIẾM SẢN PHẨM (Có phân trang & Sắp xếp)
    public List<ProductCardDTO> searchProducts(String keyword, String sort, int page, int size) {
        List<ProductCardDTO> list = null;
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            // 1. Khởi tạo câu HQL cơ bản
            String hql = "SELECT new com.example.dto.ProductCardDTO(" +
                    "p.id, p.name, p.price, img.path, s.name, 'SearchResult') " +
                    "FROM Product p " +
                    "LEFT JOIN p.productImages img " +
                    "LEFT JOIN p.sport s " +
                    "WHERE p.isDelete = false " +
                    "AND p.status = 'Đang bán' " +
                    "AND (img.isPrimary = true OR img.id IS NULL) ";

            // 2. Cộng chuỗi điều kiện Tìm kiếm (Nếu có keyword)
            if (keyword != null && !keyword.trim().isEmpty()) {
                hql += " AND LOWER(p.name) LIKE :keyword ";
            }

            // 3. Cộng chuỗi Sắp xếp
            if ("newest".equals(sort)) {
                hql += " ORDER BY p.createdAt DESC ";
            } else if ("price_asc".equals(sort)) {
                hql += " ORDER BY p.price ASC ";
            } else if ("price_desc".equals(sort)) {
                hql += " ORDER BY p.price DESC ";
            } else {
                // Mặc định: Sắp xếp theo tên (hoặc độ liên quan)
                hql += " ORDER BY p.name ASC ";
            }

            // 4. Tạo Query (Không truyền .class để tránh lỗi Hibernate 6)
            org.hibernate.query.Query<ProductCardDTO> query = session.createQuery(hql);

            // 5. Gán tham số keyword (nếu có)
            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
            }

            // 6. Phân trang (Limit / Offset)
            query.setFirstResult(page * size); // Vị trí bắt đầu
            query.setMaxResults(size);         // Số lượng lấy ra

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

    // 5. ĐẾM TỔNG SỐ KẾT QUẢ TÌM KIẾM (Để tính số trang)
    public long countSearchProducts(String keyword) {
        long count = 0;
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            // Câu HQL đếm số lượng
            String hql = "SELECT count(p.id) FROM Product p " +
                    "WHERE p.isDelete = false AND p.status = 'Đang bán' ";

            if (keyword != null && !keyword.trim().isEmpty()) {
                hql += " AND LOWER(p.name) LIKE :keyword ";
            }

            org.hibernate.query.Query<Long> query = session.createQuery(hql);

            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
            }

            // Lấy kết quả duy nhất (số lượng)
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
}