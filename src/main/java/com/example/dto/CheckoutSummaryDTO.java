package com.example.dto;

import com.example.model.CartItem;
import java.util.List;

public class CheckoutSummaryDTO {
    private List<UserCartItemDTO> cartItems;
    private Long subTotal; // Tổng tiền hàng (chưa có phí ship, chưa trừ voucher)
    private Long finalTotal; // Tổng tiền cuối cùng
    private Long deliveryFee;
    private Long discountAmount;
    private String voucherCode; // Mã voucher đã áp dụng

    public CheckoutSummaryDTO() {
        this.deliveryFee = 0L;
        this.discountAmount = 0L;
    }

    public List<UserCartItemDTO> getCartItems() { return cartItems; }
    public void setCartItems(List<UserCartItemDTO> cartItems) { this.cartItems = cartItems; }

    public Long getSubTotal() { return subTotal; }
    public void setSubTotal(Long subTotal) { this.subTotal = subTotal; }

    public Long getFinalTotal() { return finalTotal; }
    public void setFinalTotal(Long finalTotal) { this.finalTotal = finalTotal; }

    public Long getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(Long deliveryFee) { this.deliveryFee = deliveryFee; }

    public Long getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Long discountAmount) { this.discountAmount = discountAmount; }

    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
}