package com.example.dto;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public class ProductFormDTO {

    private Long id;
    private String name;
    private String description;
    private Long price;
    private Long salePrice; // Giá khuyến mãi
    private String status;
    private Integer quantity; // Cho sản phẩm không có biến thể
    private Long brandId;
    private Long categoryId;

    private MultipartFile imageFile;
    private String oldImagePath;

    private List<Long> variantId;
    private List<String> variantColor;
    private List<String> variantSize;
    private List<Integer> variantStock;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Long getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Long salePrice) {
        this.salePrice = salePrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public MultipartFile getImageFile() {
        return imageFile;
    }

    public void setImageFile(MultipartFile imageFile) {
        this.imageFile = imageFile;
    }

    public String getOldImagePath() {
        return oldImagePath;
    }

    public void setOldImagePath(String oldImagePath) {
        this.oldImagePath = oldImagePath;
    }

    public List<Long> getVariantId() {
        return variantId;
    }

    public void setVariantId(List<Long> variantId) {
        this.variantId = variantId;
    }

    public List<String> getVariantColor() {
        return variantColor;
    }

    public void setVariantColor(List<String> variantColor) {
        this.variantColor = variantColor;
    }

    public List<String> getVariantSize() {
        return variantSize;
    }

    public void setVariantSize(List<String> variantSize) {
        this.variantSize = variantSize;
    }

    public List<Integer> getVariantStock() {
        return variantStock;
    }

    public void setVariantStock(List<Integer> variantStock) {
        this.variantStock = variantStock;
    }
}