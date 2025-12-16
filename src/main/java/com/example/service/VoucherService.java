package com.example.service;

import com.example.dao.VoucherDao;
import com.example.dto.VoucherDTO;
import com.example.model.Voucher;
import com.example.dto.VoucherApplyResultDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
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

    public VoucherApplyResultDTO applyVoucher(Long userId, String code, List<Long> variantIdsInCart) throws Exception {

        if (code.equalsIgnoreCase("GIAY50")) {

            return new VoucherApplyResultDTO(true, 50000L, "Áp dụng mã GIẢY50 thành công.");
        }

        if (code.equalsIgnoreCase("ERROR")) {
            throw new Exception("Mã ERROR đã hết lượt sử dụng.");
        }

        return new VoucherApplyResultDTO(false, 0L, "Mã giảm giá không hợp lệ.");
    }
    public long calculateDiscount(Long userId, String code, Long subTotal) throws Exception {

        if (code == null || code.isEmpty()) {
            throw new Exception("Vui lòng nhập mã giảm giá.");
        }

        if (code.equalsIgnoreCase("GIAY10")) {
            if (subTotal < 500000L) {
                throw new Exception("Đơn hàng tối thiểu 500.000đ để áp dụng mã này.");
            }
            long discount = (long) (subTotal * 0.10);
            return Math.min(discount, 100000L); // Giảm tối đa 100k
        }

        if (code.equalsIgnoreCase("GIAY50K")) {
            return 50000L;
        }

        throw new Exception("Mã " + code + " không hợp lệ hoặc đã hết hạn.");
    }
}