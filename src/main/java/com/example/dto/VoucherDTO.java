package com.example.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class VoucherDTO {

    private String code;

    // Sử dụng BigDecimal cho số tiền để đảm bảo tính toán chính xác
    private Long discountAmount;

    private Long minOrderAmount;

    private Integer quantity;

    private Timestamp expiryDate;

    private String status;

    public VoucherDTO() {}

    public VoucherDTO(String code, Long discountAmount, Long minOrderAmount, Integer quantity, Timestamp expiryDate) {
        this.code = code;
        this.discountAmount = discountAmount;
        this.minOrderAmount = minOrderAmount;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
    }


    public String getCode() {
        return code;
    }

    public Long getDiscountAmount() {
        return discountAmount;
    }

    public Long getMinOrderAmount() {
        return minOrderAmount;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Timestamp getExpiryDate() {
        return expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDiscountAmount(Long discountAmount) {
        this.discountAmount = discountAmount;
    }

    public void setMinOrderAmount(Long minOrderAmount) {
        this.minOrderAmount = minOrderAmount;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setExpiryDate(Timestamp expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}