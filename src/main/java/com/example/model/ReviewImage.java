package com.example.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_images")
public class ReviewImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_review_id", nullable = false)
    private ProductReview productReview;

    @Column(length = 500)
    private String path;

    private Integer size;

    private LocalDateTime date; 

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ReviewImage() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ProductReview getProductReview() { return productReview; }
    public void setProductReview(ProductReview productReview) { this.productReview = productReview; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}