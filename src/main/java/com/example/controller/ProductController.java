package com.example.controller;

import java.util.List;

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
            return "redirect:/customer/";
        }
        List<ProductCardDTO> relatedProducts = homeService.getRelatedProducts(product.getBrandName(), id);
        request.setAttribute("product", product);
        request.setAttribute("relatedProducts", relatedProducts);
        return "customer/product_detail";
    }
}
