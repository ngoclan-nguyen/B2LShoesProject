package com.example.service;

import com.example.dao.ProductDao;
import com.example.dto.ProductCardDTO;
import com.example.dto.ProductDetailDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeService {

    @Autowired
    private ProductDao productDAO;

    public List<ProductCardDTO> getFeaturedProducts() {
        return productDAO.findFeaturedProducts(8);
    }

    public List<ProductCardDTO> getBestSellerProducts() {
        return productDAO.findBestSellerProducts(8);
    }
    
    public List<ProductCardDTO> getProductsByGender(String gender) {
        return productDAO.findProductsByGender(gender);
    }

    public List<ProductCardDTO> searchProducts(String keyword, String sort, int page) {
        int pageSize = 12;
        return productDAO.searchProducts(keyword, sort, page, pageSize);
    }

    public int getTotalPages(String keyword) {
        int pageSize = 12;
        long totalItems = productDAO.countSearchProducts(keyword);
        return (int) Math.ceil((double) totalItems / pageSize);
    }

    public ProductDetailDTO getProductDetailById(Long id) {
        return productDAO.findProductDetailById(id);
    }

    public List<ProductCardDTO> getRelatedProducts(String brandName, Long currentProductId) {
        return productDAO.findRelatedProducts(brandName, currentProductId, 5);
    }
}