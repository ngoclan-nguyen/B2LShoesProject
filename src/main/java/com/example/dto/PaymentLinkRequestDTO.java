package com.example.dto;

public class PaymentLinkRequestDTO {

    private Long orderId;
    private Long amount;
    private String description;

    public PaymentLinkRequestDTO() {
    }

    public PaymentLinkRequestDTO(Long orderId, Long amount, String description) {
        this.orderId = orderId;
        this.amount = amount;
        this.description = description;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}