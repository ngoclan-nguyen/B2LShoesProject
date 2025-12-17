package com.example.dto;

public class OrderDetailDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Long productPrice;  
    private String productBrand;
    private Long totalAmount;
    private Integer quantity;
    private String sizeName;
    private String productImage;
    
    public OrderDetailDTO() {
    }

    // ===== Constructor đầy đủ =====
    public OrderDetailDTO(Long id,
                          Long productId,
                          String productName,
                          Long productPrice,
                          String productBrand,
                          Long totalAmount,
                          Integer quantity,
                          String sizeName,
                          String productImage) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productBrand = productBrand;
        this.totalAmount = totalAmount;
        this.quantity = quantity;
        this.sizeName = sizeName;
        this.productImage = productImage;
    }

    // ===== Getter & Setter =====
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Long productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductBrand() {
        return productBrand;
    }

    public void setProductBrand(String productBrand) {
        this.productBrand = productBrand;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getSizeName() {
        return sizeName;
    }

    public void setSizeName(String sizeName) {
        this.sizeName = sizeName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }
}