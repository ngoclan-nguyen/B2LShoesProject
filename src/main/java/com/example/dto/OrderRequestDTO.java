package com.example.dto;

import java.util.List;

public class OrderRequestDTO {
    private String consignee;
    private String phoneNumber;
    private String deliveryAddress;
    private String note;
    private String paymentMethod;
    private String voucherCode;
    private List<Long> productVariantIds;

    public OrderRequestDTO() {}

    public String getConsignee() { return consignee; }
    public void setConsignee(String consignee) { this.consignee = consignee; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }

    public List<Long> getProductVariantIds() { return productVariantIds; }
    public void setProductVariantIds(List<Long> productVariantIds) { this.productVariantIds = productVariantIds; }
}