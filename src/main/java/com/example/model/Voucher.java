package com.example.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "voucher")
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;         // Mã code (GIAMGIA100)

    @Column(nullable = false, name = "discount_amount") // <--- FIX DISCOUNT AMOUNT
    private Long discountAmount;

    @Column(nullable = false, name = "min_order_amount") // <--- FIX MIN ORDER AMOUNT
    private Long minOrderAmount;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "is_active")
    private boolean isActive = true;

    public Voucher() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Long discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Long getMinOrderAmount() {
        return minOrderAmount;
    }

    public void setMinOrderAmount(Long minOrderAmount) {
        this.minOrderAmount = minOrderAmount;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}