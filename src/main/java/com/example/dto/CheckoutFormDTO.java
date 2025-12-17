package com.example.dto;

import com.example.model.Consignee;

import java.util.List;

public class CheckoutFormDTO {
    private Consignee consignee;
    private String phoneNumber;
    private String deliveryAddress;
    private String paymentMethod; // COD hoặc TRANSFER
    private String voucherCode;
    private Long finalAmount; // Tổng tiền cuối cùng (để xác thực)
    private List<Long> productVariantIds; // Thêm trường này
    private Long discountAmount;

    public Long getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Long discountAmount) { this.discountAmount = discountAmount; }
    public List<Long> getProductVariantIds() { return productVariantIds; }
    public void setProductVariantIds(List<Long> productVariantIds) { this.productVariantIds = productVariantIds; }
    public Consignee getConsignee() { return consignee; }
    public void setConsignee(Consignee consignee) {
        // Dòng này phải chấp nhận đối tượng Consignee
        this.consignee = consignee;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }

    public Long getFinalAmount() { return finalAmount; }
    public void setFinalAmount(Long finalAmount) { this.finalAmount = finalAmount; }
}