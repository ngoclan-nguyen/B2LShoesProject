package com.example.dto;

public class VoucherApplyResultDTO {

    private Long originalAmount;
    private Long discountAmount;
    private Long finalAmount;
    private String appliedVoucherCode;
    // Thuộc tính để xử lý kết quả và thông báo lỗi (Cần thiết cho AJAX)
    private boolean success;
    private String message;
    public VoucherApplyResultDTO() {}

    public VoucherApplyResultDTO(boolean success, Long discountAmount, String message) {
        this.success = success;
        this.discountAmount = discountAmount;
        this.message = message;
    }

    public VoucherApplyResultDTO(Long originalAmount, Long discountAmount, Long finalAmount, String appliedVoucherCode) {
        this.originalAmount = originalAmount;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
        this.appliedVoucherCode = appliedVoucherCode;
    }

    public Long getOriginalAmount() { return originalAmount; }
    public Long getDiscountAmount() { return discountAmount; }
    public Long getFinalAmount() { return finalAmount; }
    public String getAppliedVoucherCode() { return appliedVoucherCode; }

    public void setOriginalAmount(Long originalAmount) { this.originalAmount = originalAmount; }
    public void setDiscountAmount(Long discountAmount) { this.discountAmount = discountAmount; }
    public void setFinalAmount(Long finalAmount) { this.finalAmount = finalAmount; }
    public void setAppliedVoucherCode(String appliedVoucherCode) { this.appliedVoucherCode = appliedVoucherCode; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}