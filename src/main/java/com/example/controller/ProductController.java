package com.example.controller;

import java.util.List;

import com.example.dao.BrandDao;
import com.example.dao.ProductReviewDao;
import com.example.dao.SportDao;
import com.example.model.ProductReview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.example.dto.ProductCardDTO;
import com.example.dto.ProductDetailDTO;
import com.example.service.HomeService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ProductController {
	@Autowired
    private HomeService homeService;
    @Autowired
    private BrandDao brandDao;
    @Autowired
    private SportDao sportDao;
    @Autowired
    private ProductReviewDao productReviewDao;

    @GetMapping("/search")
    public String search(
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(name = "sort", required = false, defaultValue = "relevant") String sort,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            HttpServletRequest request) {

        List<ProductCardDTO> products = homeService.searchProducts(keyword, sort, page);
        int totalPages = homeService.getTotalPages(keyword);
        request.setAttribute("products", products);
        request.setAttribute("keyword", keyword);
        request.setAttribute("sort", sort);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        return "customer/search.html";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable("id") Long id, HttpServletRequest request) {
        ProductDetailDTO product = homeService.getProductDetailById(id);

        if (product == null) {
            System.out.println("Không tìm thấy sản phẩm có ID: " + id);
            return "redirect:/";
        }

        List<ProductReview> reviews = productReviewDao.getReviewsByProductId(id);
        request.setAttribute("reviews", reviews);
        request.setAttribute("reviewCount", reviews != null ? reviews.size() : 0);

        List<ProductCardDTO> relatedProducts = homeService.getRelatedProducts(product.getBrandName(), id);

        request.setAttribute("product", product);
        request.setAttribute("relatedProducts", relatedProducts);

        return "customer/product_detail";
    }

    @GetMapping("/products")
    public String shopPage(
            @RequestParam(required = false) List<String> gender,
            @RequestParam(required = false) List<String> sport,
            @RequestParam(required = false) List<String> brand,
            @RequestParam(required = false) List<String> category,
            @RequestParam(required = false) List<String> size,
            @RequestParam(required = false) List<String> color,
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false, defaultValue = "") String keyword,
            HttpServletRequest request) {

        List<ProductCardDTO> products = homeService.filterProducts(gender, sport, brand, category, size, color, priceRange, keyword);

        request.setAttribute("products", products);

        request.setAttribute("selectedGender", gender);
        request.setAttribute("selectedSport", sport);
        request.setAttribute("selectedBrand", brand);
        request.setAttribute("selectedSize", size);
        request.setAttribute("selectedColor", color);
        request.setAttribute("selectedPrice", priceRange);

        request.setAttribute("keyword", keyword);

        request.setAttribute("allSports", sportDao.findAll());
        request.setAttribute("allBrands", brandDao.findAll());
        request.setAttribute("pageTitle", "Sản phẩm");

        return "customer/products";
    }

    @GetMapping("/products/ajax")
    public String shopPageAjax(
            @RequestParam(required = false) List<String> gender,
            @RequestParam(required = false) List<String> sport,
            @RequestParam(required = false) List<String> brand,
            @RequestParam(required = false) List<String> category,
            @RequestParam(required = false) List<String> size,
            @RequestParam(required = false) List<String> color,
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false, defaultValue = "") String keyword,
            HttpServletRequest request) {

        List<ProductCardDTO> products = homeService.filterProducts(gender, sport, brand, category, size, color, priceRange, keyword);
        request.setAttribute("products", products);

        return "customer/products :: productList";
    }
}
