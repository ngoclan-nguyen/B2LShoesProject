package com.example.service;

import com.example.dao.CartDao;
import com.example.dao.OrderWebDao;
import com.example.dao.PaymentLogDao;
import com.example.dao.ProductVariantDao;
import com.example.dto.*;
import com.example.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderWebDao orderWebDao;

    @Autowired
    private ProductVariantDao productVariantDao;

    @Autowired
    private CartDao cartDao;

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private PaymentLogDao paymentLogDao;

    @Transactional(rollbackFor = Exception.class)
    public OrderWeb placeOrder(UserDTO customerDto, CheckoutFormDTO form) throws Exception {
        // 1. Kiểm tra session khách hàng
        if (customerDto == null || customerDto.getId() == null) {
            throw new Exception("Phiên đăng nhập hết hạn, vui lòng đăng nhập lại.");
        }

        // 2. Lấy giỏ hàng và lọc sản phẩm được chọn
        List<UserCartItemDTO> allCartItems = cartDao.getCartItemByUserId(customerDto.getId());

        // SỬA: Đảm bảo ép kiểu .longValue() khi so sánh ID để tránh lỗi khác kiểu dữ liệu
        List<UserCartItemDTO> selectedItems = allCartItems.stream()
                .filter(item -> form.getProductVariantIds() != null &&
                        form.getProductVariantIds().contains(item.getProductVariantId().longValue()))
                .collect(Collectors.toList());

        if (selectedItems.isEmpty()) {
            throw new Exception("Vui lòng chọn sản phẩm để thanh toán.");
        }

        // 3. Tính toán tiền hàng
        long subTotal = 0L;
        for (UserCartItemDTO itemDto : selectedItems) {
            subTotal += itemDto.getProductPrice() * itemDto.getQuantity().longValue();
        }

        long deliveryFee = 30000L;
        long discount = 0L;

        // 4. Áp dụng Voucher
        if (form.getVoucherCode() != null && !form.getVoucherCode().trim().isEmpty()) {
            try {
                discount = voucherService.calculateDiscount(
                        customerDto.getId(), form.getVoucherCode(), subTotal
                );
            } catch (Exception e) {
                System.err.println("DEBUG: Voucher failed: " + e.getMessage());
                discount = 0L;
            }
        }

        long finalTotal = subTotal + deliveryFee - discount;
        if (finalTotal < 0) finalTotal = 0L;

        // 5. Xác thực tổng tiền với Frontend để chống gian lận
        if (form.getFinalAmount() == null || finalTotal != form.getFinalAmount().longValue()) {
            throw new Exception("Lỗi xác thực: Tổng tiền không khớp. Vui lòng thử lại.");
        }

        // 6. Kiểm tra thông tin người nhận
        Consignee consignee = form.getConsignee();
        if (consignee == null || consignee.getFullName() == null || consignee.getPhone() == null) {
            throw new Exception("Thông tin người nhận không được bỏ trống.");
        }

        // 7. KHỞI TẠO ĐỐI TƯỢNG ORDERWEB
        OrderWeb order = new OrderWeb();

        // FIX LỖI: Column 'customer_id' cannot be null
        // Tạo mới đối tượng User và gán ID từ DTO để Hibernate map vào cột customer_id
        User userEntity = new User();
        userEntity.setId(customerDto.getId());
        order.setCustomer(userEntity);

        // Gán thông tin vận chuyển
        order.setConsignee(consignee.getFullName());
        order.setPhoneNumber(consignee.getPhone());
        order.setDeliveryAddress(form.getDeliveryAddress());

        // Trạng thái thanh toán
        String paymentMethod = form.getPaymentMethod();
        order.setPaymentMethod(paymentMethod);
        if ("COD".equalsIgnoreCase(paymentMethod)) {
            order.setPaymentStatus("UNPAID");
        } else if ("TRANSFER".equalsIgnoreCase(paymentMethod)) {
            order.setPaymentStatus("PENDING");
        } else {
            throw new Exception("Phương thức thanh toán không hợp lệ.");
        }

        order.setDeliveryStatus("PENDING");
        order.setSubTotal(subTotal);
        order.setDiscount(discount);
        order.setDeliveryFee(deliveryFee);
        order.setTotalAmount(finalTotal);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // 8. LƯU ĐƠN HÀNG (OrderWebDao)
        orderWebDao.saveOrder(order);

        // 9. LƯU CHI TIẾT ĐƠN HÀNG VÀ TRỪ TỒN KHO
        for (UserCartItemDTO itemDto : selectedItems) {
            ProductVariant variant = productVariantDao.findById(itemDto.getProductVariantId().longValue());

            if (variant == null || variant.getQuantity() < itemDto.getQuantity()) {
                throw new Exception("Sản phẩm " + itemDto.getProductName() + " không đủ tồn kho.");
            }

            OrderWebDetail detail = new OrderWebDetail();
            detail.setOrderWeb(order);
            detail.setProductVariant(variant);
            detail.setQuantity(itemDto.getQuantity());
            detail.setPrice(itemDto.getProductPrice());
            detail.setTotalAmount(itemDto.getProductPrice() * itemDto.getQuantity().longValue());

            // Cập nhật kho
            variant.setQuantity(variant.getQuantity() - itemDto.getQuantity());
            productVariantDao.update(variant);

            // Xóa khỏi giỏ hàng
            cartDao.deleteCartItem(customerDto.getId(), itemDto.getProductVariantId().longValue());
        }

        return order;
    }
    /**
     * Lấy danh sách đơn hàng theo Customer ID.
     */
    public List<OrderWeb> getOrdersByCustomerId(Long customerId) {
        return orderWebDao.findByCustomerId(customerId);
    }

    /**
     * Lấy chi tiết đơn hàng (bao gồm logic gán ảnh đại diện).
     */
    public OrderWeb getOrderDetail(Long orderId, Long customerId) {
        OrderWeb order = orderWebDao.findByIdAndCustomerId(orderId, customerId);
        if (order != null) {
            for (OrderWebDetail detail : order.getOrderDetails()) {
                Product product = detail.getProductVariant().getProduct();

                // Log giá trị để kiểm tra
                System.out.println("Product ID: " + product.getId());
                if (product.getProductImages() != null) {
                    System.out.println("Total Images: " + product.getProductImages().size());
                }

                boolean primaryFound = false;
                if (product.getProductImages() != null) {
                    for (ProductImage img : product.getProductImages()) {
                        if (img.getIsPrimary()) {
                            product.setImage(img.getPath());
                            primaryFound = true;
                            break;
                        }
                    }
                }

                if (!primaryFound) {
                    product.setImage("default/no_image.jpg");
                }
                System.out.println("Final Image Path: " + product.getImage());
            }
        }
        return order;
    }

    public OrderWeb getOrderById(Long orderId) {
        return orderWebDao.findOrderById(orderId);
    }

    @Transactional
    public void updatePaymentStatus(Long orderId, String status) {
        OrderWeb order = orderWebDao.findOrderById(orderId);

        if (order != null) {
            order.setPaymentStatus(status);
            order.setUpdatedAt(LocalDateTime.now());

            orderWebDao.saveOrder(order);
        }
    }
}