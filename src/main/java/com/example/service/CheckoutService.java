package com.example.service;

import com.example.dao.CartDao;
import com.example.dao.ProductDao;
import com.example.dto.CheckoutSummaryDTO;
import com.example.dto.UserCartItemDTO;
import com.example.model.Product;
import com.example.model.ProductImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CheckoutService {

    @Autowired
    private CartDao cartDao;

    @Autowired
    private VoucherService voucherService;

    public CheckoutSummaryDTO getCheckoutSummary(Long userId, String selectedIdsString, String voucherCode) {

        CheckoutSummaryDTO summary = new CheckoutSummaryDTO();
        long subTotal = 0L;

        // 1. Phân tích chuỗi ID đã chọn và tải các mục trong giỏ hàng
        List<Long> selectedVariantIds = Arrays.stream(selectedIdsString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());

        // Lấy toàn bộ giỏ hàng và lọc theo ID đã chọn
        // Giả định cartDao.getCartItemsByCustomerId() lấy được dữ liệu cần thiết
        List<UserCartItemDTO> allCartItems = cartDao.getCartItemByUserId(userId);
        List<UserCartItemDTO> selectedItems = allCartItems.stream()
                .filter(item -> selectedVariantIds.contains(item.getProductVariantId().longValue()))
                .collect(Collectors.toList());

        // 2. Tính SubTotal
        for (UserCartItemDTO item : selectedItems) {
            subTotal += item.getProductPrice() * item.getQuantity();
        }

        summary.setCartItems(selectedItems);
        summary.setSubTotal(subTotal);

        long discountAmount = 0L;

        // 3. Xử lý Voucher (Nếu mã voucher được gửi từ Giỏ hàng)
        if (voucherCode != null && !voucherCode.isEmpty()) {
            try {
                // Gọi VoucherService để tính toán giảm giá thực tế (dựa trên subTotal)
                discountAmount = voucherService.calculateDiscount(userId, voucherCode, subTotal);

                // Nếu tính toán thành công:
                summary.setVoucherCode(voucherCode);
                summary.setDiscountAmount(discountAmount);

            } catch (Exception e) {
                // Nếu Voucher không còn hợp lệ khi chuyển trang, discount = 0 và bỏ qua mã
                System.err.println("Voucher " + voucherCode + " không hợp lệ khi tải trang checkout: " + e.getMessage());
                summary.setVoucherCode(null);
                summary.setDiscountAmount(0L);
            }
        }

        // 4. Tính Final Total
        summary.setDeliveryFee(summary.getDeliveryFee()); // Giữ nguyên phí cố định
        long finalTotal = (summary.getSubTotal() + summary.getDeliveryFee()) - summary.getDiscountAmount();
        summary.setFinalTotal(finalTotal < 0 ? 0L : finalTotal);

        return summary;
    }
}