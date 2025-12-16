package com.example.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "price")
    private Long price;

    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "status", length = 45)
    private String status;

    @Column(name = "is_delete")
    private Boolean isDelete;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "product_group_id")
    private ProductGroup productGroup;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_brand_id")
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "product_sport_id")
    private Sport sport;

    @ManyToOne
    @JoinColumn(name = "category")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "color_id")
    private Color color;

    @Column(name = "sale_price")
    private Long salePrice;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProductVariant> variants;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProductImage> productImages;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<ProductReview> reviews;

    public Product() {}

    public Product(Long id, String name, Long price, Integer quantity, String status, Boolean isDelete) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.status = status;
        this.isDelete = isDelete;
    }

    public Long getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Long salePrice) {
        this.salePrice = salePrice;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getIsDelete() { return isDelete; }
    public void setIsDelete(Boolean isDelete) { this.isDelete = isDelete; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public ProductGroup getProductGroup() { return productGroup; }
    public void setProductGroup(ProductGroup productGroup) { this.productGroup = productGroup; }

    public Brand getBrand() { return brand; }
    public void setBrand(Brand brand) { this.brand = brand; }

    public Sport getSport() { return sport; }
    public void setSport(Sport sport) { this.sport = sport; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }

    public List<ProductVariant> getVariants() { return variants; }
    public void setVariants(List<ProductVariant> variants) { this.variants = variants; }

    public List<ProductImage> getProductImages() {
        return productImages;
    }

    public void setProductImages(List<ProductImage> productImages) {
        this.productImages = productImages;
    }

    @Transient
    private String image; // Biến tạm để lưu link ảnh đại diện hiển thị ra view

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public void addImage(ProductImage image) {
        // Nếu list chưa có thì tạo mới ngay lập tức
        if (this.productImages == null) {
            this.productImages = new ArrayList<>();
        }

        this.productImages.add(image);
        image.setProduct(this);
    }

    public List<ProductReview> getReviews() { return reviews; }
    public void setReviews(List<ProductReview> reviews) { this.reviews = reviews; }
}