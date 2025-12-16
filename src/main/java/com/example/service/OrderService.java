package com.example.service;

import com.example.dao.CartDao;
import com.example.dao.OrderWebDao;
import com.example.dao.ProductVariantDao;
import com.example.dto.*;
import com.example.model.*;
import org.springframework.beans.factory.annotation.Autowired;
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

    // Giả định: Các DAO khác cần thiết
    // @Autowired private OrderWebDetailDao orderWebDetailDao;
    // @Autowired private PaymentLogDao paymentLogDao;
    // @Autowired private DeliveryLogDao deliveryLogDao;


    /**
     * Hàm đặt hàng cũ (Dùng cho API hoặc luồng cũ), nhận OrderRequestDTO.
     */
    @Transactional(rollbackFor = Exception.class)
    public Long placeOrder(User customer, OrderRequestDTO request) throws Exception {

        // 1. Khởi tạo đơn hàng (OrderWeb)
        OrderWeb order = new OrderWeb();
        order.setCustomer(customer);
        order.setConsignee(request.getConsignee());
        order.setPhoneNumber(request.getPhoneNumber());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setPaymentMethod(request.getPaymentMethod());

        // Thiết lập trạng thái mặc định
        order.setPaymentStatus("UNPAID");
        order.setDeliveryStatus("PENDING");
        order.setDeliveryFee(0L);

        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        // Lấy danh sách biến thể sản phẩm từ ID gửi lên
        List<ProductVariant> variants = new ArrayList<>();
        for (Long id : request.getProductVariantIds()) {
            // Giả định productVariantDao.findById nhận Long
            ProductVariant v = productVariantDao.findById(id);
            if (v != null) variants.add(v);
        }

        if (variants.isEmpty()) {
            throw new Exception("Không tìm thấy sản phẩm hợp lệ.");
        }

        // Xử lý chi tiết đơn hàng (OrderWebDetail)
        long subTotal = 0L;
        List<OrderWebDetail> details = new ArrayList<>();

        for (ProductVariant variant : variants) {
            // Giả định cartDao.getCartItemByUserAndVariant nhận Long, Long
            CartItem cartItem = cartDao.getCartItemByUserAndVariant(
                    customer.getId(),
                    variant.getId().longValue()
            );

            if (cartItem == null) continue;

            Integer quantity = cartItem.getQuantity();

            // Kiểm tra tồn kho
            if (variant.getQuantity() < quantity) {
                throw new Exception("Sản phẩm " + variant.getId() + " không đủ số lượng tồn kho.");
            }

            // Trừ tồn kho và cập nhật vào DB
            variant.setQuantity(variant.getQuantity() - quantity);
            productVariantDao.update(variant);

            // Tạo chi tiết đơn hàng
            OrderWebDetail detail = new OrderWebDetail();
            detail.setOrderWeb(order);
            detail.setProductVariant(variant);
            detail.setQuantity(quantity);

            // Lấy giá và ép kiểu an toàn về Long
            Long price = variant.getProduct().getPrice().longValue();
            detail.setPrice(price);
            detail.setQuantity(quantity);

            // Tính thành tiền (price * quantity)
            Long lineTotal = price * quantity.longValue();

            detail.setTotalAmount(lineTotal);
            detail.setCreatedAt(now);
            detail.setUpdatedAt(now);

            details.add(detail);
            subTotal += lineTotal;
        }

        if (details.isEmpty()) {
            throw new Exception("Giỏ hàng trống hoặc lỗi sản phẩm.");
        }

        // Gán danh sách chi tiết vào đơn hàng
        order.setOrderDetails(details);

        // Xử lý Voucher
        long discountAmount = 0L;
        if (request.getVoucherCode() != null && !request.getVoucherCode().isEmpty()) {
            try {
                VoucherApplyResultDTO result = voucherService.applyVoucher(
                        customer.getId(), request.getVoucherCode(), request.getProductVariantIds()
                );
                discountAmount = result.getDiscountAmount();
            } catch (Exception e) {
                // Log lỗi voucher nhưng vẫn cho phép đặt hàng (không giảm giá)
                System.out.println("Lỗi áp dụng voucher: " + e.getMessage());
            }
        }

        // Tính tổng tiền cuối cùng
        long finalTotal = (subTotal + order.getDeliveryFee()) - discountAmount;
        if (finalTotal < 0) finalTotal = 0L;

        order.setSubTotal(subTotal);
        order.setDiscount(discountAmount);

        order.setTotalAmount(finalTotal);
        // Lưu đơn hàng vào DB
        Long orderId = orderWebDao.saveOrder(order);

        // Xóa các sản phẩm đã mua khỏi giỏ hàng
        for (Long variantId : request.getProductVariantIds()) {
            cartDao.deleteCartItem(customer.getId(), variantId);
        }

        return orderId;
    }

    /**
     * Hàm đặt hàng mới (Dùng cho luồng Checkout Page), nhận UserDTO và CheckoutFormDTO.
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderWeb placeOrder(UserDTO customerDto, CheckoutFormDTO form) throws Exception {

        // 1. Tải lại giỏ hàng và lọc theo ID đã chọn
        // Giả định cartDao.getCartItemsByCustomerId trả về List<UserCartItemDTO>
        List<UserCartItemDTO> allCartItems = cartDao.getCartItemByUserId(customerDto.getId());

        if (allCartItems.isEmpty()) {
            throw new Exception("Giỏ hàng trống.");
        }

        long subTotal = 0L;
        // Lấy ProductVariant IDs từ Cart Items (sẽ cần trong bước trừ tồn kho)
        List<Long> variantIdsInCart = allCartItems.stream()
                .map(UserCartItemDTO::getProductVariantId)
                .map(Integer::longValue)
                .collect(Collectors.toList());

        for (UserCartItemDTO itemDto : allCartItems) {
            // Dùng giá trị từ DTO
            subTotal += itemDto.getProductPrice() * itemDto.getQuantity().longValue();
        }

        long deliveryFee = 30000L;
        long discount = 0L;

        // Xử lý Voucher (Nếu có)
        if (form.getVoucherCode() != null && !form.getVoucherCode().isEmpty()) {
            try {
                VoucherApplyResultDTO result = voucherService.applyVoucher(
                        customerDto.getId(), form.getVoucherCode(), variantIdsInCart
                );
                discount = result.getDiscountAmount();
            } catch (Exception e) {
                System.err.println("Lỗi áp dụng voucher: " + e.getMessage());
                // Không rollback, chỉ bỏ qua giảm giá
            }
        }

        long finalTotal = subTotal + deliveryFee - discount;
        if (finalTotal < 0) finalTotal = 0L;

        if (finalTotal != (form.getFinalAmount())) {
            throw new Exception("Lỗi xác thực tổng tiền. Vui lòng thử lại.");
        }

        // 3. TẠO ORDERWEB
        OrderWeb order = new OrderWeb();
        // Khởi tạo User Entity chỉ với ID (Giả định User có constructor nhận Long ID)
        order.setCustomer(new User(customerDto.getId()));
        order.setConsignee(form.getConsignee());
        order.setPhoneNumber(form.getPhoneNumber());
        order.setDeliveryAddress(form.getDeliveryAddress());
        order.setPaymentMethod(form.getPaymentMethod());

        // Thiết lập trạng thái
        order.setPaymentStatus(form.getPaymentMethod().equals("COD") ? "UNPAID" : "PENDING");
        order.setDeliveryStatus("PENDING");

        // Gán các giá trị đã tính toán
        order.setSubTotal(subTotal);
        order.setDiscount(discount);
        order.setDeliveryFee(deliveryFee);
        order.setTotalAmount(finalTotal);

        order.setCreatedAt(LocalDateTime.now());
        orderWebDao.saveOrder(order);

        // 4. TẠO ORDERWEB DETAILS & TRỪ TỒN KHO
        for (UserCartItemDTO itemDto : allCartItems) {
            // Lấy lại ProductVariant Entity để trừ tồn kho (cần transaction)
            // Giả định productVariantDao.findById nhận Long
            ProductVariant variant = productVariantDao.findById(itemDto.getProductVariantId().longValue());

            if (variant.getQuantity() < itemDto.getQuantity()) {
                throw new Exception("Sản phẩm " + itemDto.getProductName() + " không đủ tồn kho.");
            }

            // Tạo chi tiết OrderWebDetail
            OrderWebDetail detail = new OrderWebDetail();
            detail.setOrderWeb(order);
            detail.setProductVariant(variant);
            detail.setQuantity(itemDto.getQuantity());
            detail.setPrice(itemDto.getProductPrice());
            detail.setTotalAmount(itemDto.getProductPrice() * itemDto.getQuantity().longValue());
            // orderWebDetailDao.save(detail); // Cần lưu chi tiết

            // TRỪ TỒN KHO
            variant.setQuantity(variant.getQuantity() - itemDto.getQuantity());
            productVariantDao.update(variant); // Giả định có hàm update
        }

        // 5. TẠO LOGS (PaymentLog, DeliveryLog)
        // (Bạn có thể thêm logic tạo PaymentLog và DeliveryLog tại đây)

        // 6. XÓA GIỎ HÀNG (Chỉ xóa những item đã được đặt)
        for (UserCartItemDTO itemDto : allCartItems) {
            // Giả định cartDao.deleteCartItem nhận userId (Long) và variantId (Long)
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
}