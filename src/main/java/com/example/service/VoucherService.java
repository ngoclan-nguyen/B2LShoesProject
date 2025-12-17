package com.example.service;

import com.example.dao.VoucherDao;
import com.example.dto.VoucherDTO;
import com.example.model.Voucher;
import com.example.dto.VoucherApplyResultDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VoucherService {

    @Autowired
    private VoucherDao voucherDao;

    @Autowired
    private CartService cartService;

    public List<VoucherDTO> getAvailableVouchersForUser(Long userId) {
        List<Voucher> allVouchers = voucherDao.findAll();

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        return allVouchers.stream()
                .map(v -> {
                    VoucherDTO dto = new VoucherDTO(
                            v.getCode(),
                            v.getDiscountAmount(),
                            v.getMinOrderAmount(),
                            v.getQuantity(),
                            java.sql.Timestamp.valueOf(v.getExpiryDate())
                    );

                    if (v.getQuantity() <= 0) {
                        dto.setStatus("USED"); // Hết lượt
                    }

                    else if (v.getExpiryDate().isBefore(now)) {
                        dto.setStatus("EXPIRED"); // Hết hạn
                    }

                    else {
                        dto.setStatus("ACTIVE");
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public boolean checkIfUserHasAvailableVouchers(Long userId) {
        final Instant now = Instant.now();

        return voucherDao.findAll().stream()
                .anyMatch(v ->
                        v.isActive() &&
                                v.getQuantity() > 0 &&
                                (v.getExpiryDate() == null || !v.getExpiryDate().toInstant(ZoneOffset.UTC).isBefore(now))
                );
    }

    public VoucherApplyResultDTO applyVoucher(Long userId, String voucherCode, List<Long> productVariantIds) throws Exception {

        if (voucherCode == null || voucherCode.trim().isEmpty()) {
            throw new Exception("Mã khuyến mãi không được để trống.");
        }

        Voucher voucher = voucherDao.findByCodeIgnoreCase(voucherCode);
        if (voucher == null) {
            throw new Exception("Mã khuyến mãi không tồn tại.");
        }

        // 1. Kiểm tra tính hợp lệ cơ bản
        if (!voucher.isActive()) {
            throw new Exception("Mã khuyến mãi không được kích hoạt.");
        }
        if (voucher.getQuantity() == null || voucher.getQuantity() <= 0) {
            throw new Exception("Mã khuyến mãi đã hết lượt sử dụng.");
        }

        if (voucher.getExpiryDate() != null) {
            Instant expiryInstant = voucher.getExpiryDate().toInstant(ZoneOffset.UTC);
            if (expiryInstant.isBefore(Instant.now())) {
                throw new Exception("Mã khuyến mãi đã hết hạn sử dụng.");
            }
        }

        // Tính toán Tổng tiền đơn hàng (CartService đã trả về Long)
        Long originalAmount = cartService.calculateTotalAmount(userId, productVariantIds);

        // Kiểm tra số tiền âm hoặc bằng 0
        if (originalAmount <= 0) {
            throw new Exception("Giỏ hàng chưa có sản phẩm hợp lệ hoặc chưa chọn sản phẩm.");
        }

        // Kiểm tra điều kiện đơn hàng tối thiểu
        // (So sánh Long dùng toán tử < thay vì .compareTo)
        if (originalAmount < voucher.getMinOrderAmount()) {
            String requiredAmount = new java.text.DecimalFormat("#,###").format(voucher.getMinOrderAmount()) + "đ";
            throw new Exception("Đơn hàng chưa đạt giá trị tối thiểu " + requiredAmount + ".");
        }

        // Lấy giá trị giảm giá
        Long discount = voucher.getDiscountAmount();

        // Đảm bảo không giảm quá số tiền gốc
        if (discount > originalAmount) {
            discount = originalAmount;
        }

        Long finalAmount = originalAmount - discount;

        return new VoucherApplyResultDTO(originalAmount, discount, finalAmount, voucherCode);
    }

    public long calculateDiscount(Long userId, String voucherCode, long subTotal) throws Exception {
        // 1. Tìm voucher trong Database
        Voucher voucher = voucherDao.findByCodeIgnoreCase(voucherCode);

        if (voucher == null) {
            System.out.println("VOUCHER FAIL: Mã " + voucherCode + " không tồn tại.");
            throw new Exception("Mã voucher không tồn tại.");
        }

        // 2. Kiểm tra các điều kiện hợp lệ cơ bản
        if (!voucher.isActive()) {
            throw new Exception("Mã voucher hiện đang bị khóa.");
        }

        if (voucher.getQuantity() != null && voucher.getQuantity() <= 0) {
            throw new Exception("Mã voucher đã hết lượt sử dụng.");
        }

        // Kiểm tra ngày hết hạn
        if (voucher.getExpiryDate() != null && java.time.LocalDateTime.now().isAfter(voucher.getExpiryDate())) {
            throw new Exception("Mã voucher đã hết hạn sử dụng.");
        }

        // 3. Kiểm tra điều kiện giá trị đơn hàng tối thiểu
        if (subTotal < voucher.getMinOrderAmount()) {
            throw new Exception("Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã này.");
        }

        // 4. Tính toán giảm giá (Dựa trên model Voucher hiện tại của bạn)
        // Vì model của bạn chỉ có discountAmount (số tiền cố định)
        long discountAmount = voucher.getDiscountAmount();

        // Đảm bảo số tiền giảm không vượt quá tổng tiền hàng
        if (discountAmount > subTotal) {
            discountAmount = subTotal;
        }

        System.out.println("VOUCHER SUCCESS: Discount calculated: " + discountAmount);
        return discountAmount;
    }
}