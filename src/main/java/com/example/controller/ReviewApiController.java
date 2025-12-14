package com.example.controller;

import com.example.dao.ProductDao;
import com.example.dao.ProductReviewDao;
import com.example.model.Product;
import com.example.model.ProductReview;
import com.example.model.ReviewImage;
import com.example.model.User;
import com.example.dto.UserDTO;
import com.example.dao.UserDao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/review")
public class ReviewApiController {

    @Autowired
    private ProductReviewDao reviewDao;
    @Autowired
    private ProductDao productDao;
    @Autowired
    private UserDao userDao;

    private static final String UPLOAD_DIR = "uploads/reviews/";

    @PostMapping("/add")
    public ResponseEntity<?> addReview(
            @RequestParam("productId") Long productId,
            @RequestParam("rating") Short rating,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "images", required = false) List<MultipartFile> imageFiles,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        HttpSession session = request.getSession();

        UserDTO currentUserDTO = (UserDTO) session.getAttribute("currentCustomer");
        if (currentUserDTO == null) {
            response.put("status", "error");
            response.put("message", "auth_required");
            return ResponseEntity.ok(response);
        }
        User currentUser = userDao.findUserById(currentUserDTO.getId());

        if (rating < 1 || rating > 5) {
            response.put("status", "error");
            response.put("message", "Số sao không hợp lệ!");
            return ResponseEntity.ok(response);
        }

        try {
            Product product = productDao.findById(productId);
            if(product != null) {
                ProductReview review = new ProductReview();
                review.setCustomer(currentUser);
                review.setProduct(product);
                review.setRating(rating);
                review.setTitle(title);
                review.setContent(content);
                review.setCreatedAt(LocalDateTime.now());
                review.setUpdatedAt(LocalDateTime.now());

                List<ReviewImage> reviewImages = new ArrayList<>();
                if (imageFiles != null && !imageFiles.isEmpty()) {
                    // Tạo thư mục nếu chưa có
                    Path uploadPath = Paths.get(UPLOAD_DIR);
                    if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

                    for (MultipartFile file : imageFiles) {
                        if (!file.isEmpty()) {
                            String fileName = System.currentTimeMillis() + "_" + StringUtils.cleanPath(file.getOriginalFilename());
                            Path filePath = uploadPath.resolve(fileName);
                            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                            // Tạo Entity ReviewImage
                            ReviewImage img = new ReviewImage();
                            img.setPath("/uploads/reviews/" + fileName);
                            img.setSize((int) file.getSize());
                            img.setDate(LocalDateTime.now());
                            img.setCreatedAt(LocalDateTime.now());
                            img.setUpdatedAt(LocalDateTime.now());
                            img.setProductReview(review);

                            reviewImages.add(img);
                        }
                    }
                }
                review.setImages(reviewImages); // Hibernate Cascade sẽ tự lưu Images

                boolean isSaved = reviewDao.save(review);
                if (isSaved) response.put("status", "success");
                else {
                    response.put("status", "error");
                    response.put("message", "Lỗi lưu dữ liệu.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Lỗi upload ảnh: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}