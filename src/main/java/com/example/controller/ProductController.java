package com.example.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.example.dto.ProductCardDTO;
import com.example.service.HomeService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/product")
public class ProductController {
	@Autowired
    private HomeService homeService;

    @GetMapping("/search")
    public String search(
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(name = "sort", required = false, defaultValue = "relevant") String sort,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            HttpServletRequest request) {

        // 1. Lấy dữ liệu từ Service
        List<ProductCardDTO> products = homeService.searchProducts(keyword, sort, page);
        int totalPages = homeService.getTotalPages(keyword);

        // 2. Đẩy dữ liệu ra View
        request.setAttribute("products", products);
        request.setAttribute("keyword", keyword);
        request.setAttribute("sort", sort);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        return "customer/search.html"; // Trả về file search.html
    }

    // Mapping cho đường dẫn /products
    @GetMapping("/products")
    public String shopPage(@RequestParam(name = "gender", required = false) String gender, HttpServletRequest request) {
        
        // 1. Gọi hàm tìm kiếm theo giới tính (Hàm này gọi xuống DAO bạn vừa viết)
        List<ProductCardDTO> products = homeService.getProductsByGender(gender);
        
        // 2. Đẩy danh sách sản phẩm sang giao diện
        request.setAttribute("products", products);
        
        // 3. Xử lý tiêu đề trang cho đẹp (Tùy chọn)
        String title = "Tất cả sản phẩm";
        if ("Nam".equalsIgnoreCase(gender)) {
            title = "Giày Nam";
        } else if ("Nu".equalsIgnoreCase(gender)) {
            title = "Giày Nữ";
        }
        request.setAttribute("pageTitle", title);

        // 4. Trả về file giao diện danh sách (shop.html)
        return "customer/shop"; 
    }
}
