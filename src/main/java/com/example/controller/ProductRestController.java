package com.example.controller;

import com.example.dao.ProductDao;
import com.example.dto.ProductCardDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {

    @Autowired
    private ProductDao productDao;

    // API: /api/products/suggest?keyword=adi
    @GetMapping("/suggest")
    public ResponseEntity<List<ProductCardDTO>> getSuggestions(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().length() < 2) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(productDao.searchSuggestions(keyword));
    }
    
    @GetMapping("/getSize")
	@ResponseBody
	public List<String> getProductSizeById(@RequestParam Long productId) {
	    return productDao.getProductSizeById(productId);
	}
}