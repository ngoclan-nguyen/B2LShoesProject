package com.example.service;

import com.example.dao.CartDao;
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

    private static final Long DEFAULT_DELIVERY_FEE = 30000L;

    /**
     * Lấy tóm tắt giỏ hàng, chỉ bao gồm các ProductVariantId đã được chọn.
     * @param customerId ID khách hàng
     * @param selectedIdsString Chuỗi ID ProductVariant được chọn (ví dụ: "1,5,8")
     */
    public CheckoutSummaryDTO getCheckoutSummary(Long customerId, String selectedIdsString) {

        List<Long> selectedVariantIds = Arrays.stream(selectedIdsString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());

        // Lấy tất cả Cart Items của khách hàng
        List<UserCartItemDTO> allItemsDto = cartDao.getCartItemByUserId(customerId);

        // Lọc ra các item đã được chọn
        List<UserCartItemDTO> selectedItems = allItemsDto.stream()
                .filter(item -> selectedVariantIds.contains(item.getProductVariantId().longValue()))
                .collect(Collectors.toList());

        CheckoutSummaryDTO summary = new CheckoutSummaryDTO();
        summary.setCartItems(selectedItems);

        long subTotal = 0L;

        // Tính SubTotal và gán ảnh (Logic gán ảnh phải được chuyển sang CartDao/DTO nếu có thể)
        for (UserCartItemDTO itemDto : selectedItems) {
            Long price = itemDto.getProductPrice();
            Integer quantity = itemDto.getQuantity();

            if (price != null && quantity != null) {
                subTotal += price * quantity.longValue();
            }
            // Logic gán ảnh đã được thực hiện trong CartDao hoặc DTO constructor
        }

        summary.setSubTotal(subTotal);
        summary.setDeliveryFee(DEFAULT_DELIVERY_FEE);
        summary.setDiscountAmount(0L);
        summary.setFinalTotal(subTotal + DEFAULT_DELIVERY_FEE);

        return summary;
    }
}