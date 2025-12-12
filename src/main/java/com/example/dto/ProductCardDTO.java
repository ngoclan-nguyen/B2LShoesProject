package com.example.dto;

public class ProductCardDTO {
    private Long id;
    private String name;
    private Long price;
    private String image;   
    private String categoryName;
    private String label;
    private String brand;

    public ProductCardDTO(Long id, String name, Long price, String image, String categoryName, String label) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.image = image;
        this.categoryName = categoryName;
        this.label = label;
    }

    public ProductCardDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
}
