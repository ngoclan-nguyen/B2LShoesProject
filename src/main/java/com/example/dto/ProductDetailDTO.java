package com.example.dto;

import java.util.List;

public class ProductDetailDTO {
    private Long id;
    private String name;
    private Long price;
    private String description;
    private String categoryName;
    private String brandName;

    private List<String> images;
    private List<SizeOption> sizes;

    public ProductDetailDTO(Long id, String name, Long price, String description, String categoryName, String brandName) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.categoryName = categoryName;
        this.brandName = brandName;
    }

    public static class SizeOption {
        private String name;
        private int quantity;

        public SizeOption(String name, int quantity) {
            this.name = name;
            this.quantity = quantity;
        }

        public String getName() { return name; }
        public int getQuantity() { return quantity; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public List<SizeOption> getSizes() { return sizes; }
    public void setSizes(List<SizeOption> sizes) { this.sizes = sizes; }
}