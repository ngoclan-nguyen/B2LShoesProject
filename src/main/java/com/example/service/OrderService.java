package com.example.service;

import com.example.dao.OrderDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    @Autowired private OrderDao orderDao;

    public List<?> getOrders(Long customerId) {
        return orderDao.findByCustomer(customerId);
    }

    public Object getOrderDetail(Long orderId, Long customerId) {
        return orderDao.findByIdAndCustomer(orderId, customerId);
    }

    public Long placeOrder(
            Long customerId,
            String consignee,
            String phone,
            String deliveryAddress,
            Long deliveryFee,
            String paymentMethod,
            String paymentStatus,
            String deliveryStatus
    ) {
        return orderDao.createOrderFromCart(
                customerId, consignee, phone, deliveryAddress,
                paymentMethod, deliveryFee
        );
    }

    public void cancel(Long orderId, Long customerId) {
        orderDao.cancelOrder(orderId, customerId);
    }
}
