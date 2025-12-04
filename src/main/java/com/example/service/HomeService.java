package com.example.service;

import com.example.dao.ProductDao; // Import class DAO vừa viết
import com.example.dto.ProductCardDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeService {

    @Autowired
    private ProductDao productDAO; // Tiêm DAO vào đây

    public List<ProductCardDTO> getFeaturedProducts() {
        // Lấy 8 sản phẩm mới nhất
        return productDAO.findFeaturedProducts(8);
    }

    public List<ProductCardDTO> getBestSellerProducts() {
        // Lấy 8 sản phẩm bán chạy
        return productDAO.findBestSellerProducts(8);
    }
    
    public List<ProductCardDTO> getProductsByGender(String gender) {
        return productDAO.findProductsByGender(gender);
    }

    public List<ProductCardDTO> searchProducts(String keyword, String sort, int page) {
        int pageSize = 12; // Hiển thị 12 sản phẩm mỗi trang
        return productDAO.searchProducts(keyword, sort, page, pageSize);
    }

    public int getTotalPages(String keyword) {
        int pageSize = 12;
        long totalItems = productDAO.countSearchProducts(keyword);
        return (int) Math.ceil((double) totalItems / pageSize);
    }
}