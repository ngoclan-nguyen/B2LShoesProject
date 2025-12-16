package com.example.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_web_detail")
public class OrderWebDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "price")
    private Long price;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "total_amount")
    private Long totalAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "order_web_id", nullable = false)
    private OrderWeb orderWeb;

    @ManyToOne
    @JoinColumn(name = "product_variant_id", nullable = false)
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    private ProductVariant productVariant;

    public OrderWebDetail() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Long getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Long totalAmount) { this.totalAmount = totalAmount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public OrderWeb getOrderWeb() { return orderWeb; }
    public void setOrderWeb(OrderWeb orderWeb) { this.orderWeb = orderWeb; }

    public ProductVariant getProductVariant() { return productVariant; }
    public void setProductVariant(ProductVariant productVariant) { this.productVariant = productVariant; }
}