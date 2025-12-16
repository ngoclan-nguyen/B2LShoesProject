package com.example.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "order_web")
public class OrderWeb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consignee")
    private String consignee;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "delivery_fee")
    private Long deliveryFee;

    @Column(name = "total_amount")
    private Long totalAmount;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "delivery_status")
    private String deliveryStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer; // Khớp với mappedBy="customer" ở User.java

    @OneToMany(mappedBy = "orderWeb", cascade = CascadeType.ALL)
    private List<OrderWebDetail> orderDetails;

    public OrderWeb() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getConsignee() { return consignee; }
    public void setConsignee(String consignee) { this.consignee = consignee; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Long getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(Long deliveryFee) { this.deliveryFee = deliveryFee; }

    public Long getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Long totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }

    public List<OrderWebDetail> getOrderDetails() { return orderDetails; }
    public void setOrderDetails(List<OrderWebDetail> orderDetails) { this.orderDetails = orderDetails; }

    @Transient // Báo cho Hibernate biết đây không phải là cột DB
    public Long getSubTotal() {
        if (this.orderDetails == null || this.orderDetails.isEmpty()) {
            return 0L;
        }

        return this.orderDetails.stream()
                .mapToLong(detail -> detail.getPrice() * detail.getQuantity())
                .sum();
    }
    @Transient
    public Long getDiscount() {

        Long subTotal = this.getSubTotal();
        Long deliveryFee = this.getDeliveryFee() != null ? this.getDeliveryFee() : 0L;
        Long totalAmount = this.getTotalAmount() != null ? this.getTotalAmount() : 0L;

        // Giảm giá = (Tổng tiền hàng + Phí ship) - Tổng tiền cuối cùng
        Long calculatedDiscount = (subTotal + deliveryFee) - totalAmount;

        return Math.max(0L, calculatedDiscount);
    }
}