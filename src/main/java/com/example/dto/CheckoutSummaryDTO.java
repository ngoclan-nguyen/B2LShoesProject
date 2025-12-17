package com.example.dto;

import java.util.List;

public class CheckoutSummaryDTO {

    // Thông tin sản phẩm
    private List<UserCartItemDTO> cartItems;

    // Thông tin tiền tệ
    private long subTotal;
    private long deliveryFee = 30000L; // Giả định phí ship cố định
    private long discountAmount = 0L;
    private long finalTotal;

    // Thông tin Voucher
    private String voucherCode; // Mã voucher đã áp dụng thành công

    public CheckoutSummaryDTO() {
    }

    public List<UserCartItemDTO> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<UserCartItemDTO> cartItems) {
        this.cartItems = cartItems;
    }

    public long getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(long subTotal) {
        this.subTotal = subTotal;
    }

    public long getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(long deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public long getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(long discountAmount) {
        this.discountAmount = discountAmount;
    }

    public long getFinalTotal() {
        return finalTotal;
    }

    public void setFinalTotal(long finalTotal) {
        this.finalTotal = finalTotal;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }
}