package com.example.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.dto.UserDTO;
import com.example.model.CartItem;
import com.example.model.OrderWeb;
import com.example.service.CartService;
import com.example.service.OrderService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class OrderController {

    @Autowired private CartService cartService;
    @Autowired private OrderService orderService;

    // ========== CHECKOUT PAGE ==========
    @GetMapping("/checkout")
    public String checkoutPage(HttpServletRequest request) {
        UserDTO user = (UserDTO) request.getSession().getAttribute("currentCustomer");
        if (user == null) return "redirect:/login";

        List<CartItem> items = cartService.getItemsByUser(user.getId());
        request.setAttribute("cartItems", items);

        // default thông tin nhận hàng (lấy từ session)
        request.setAttribute("defaultName", user.getName());
        request.setAttribute("defaultPhone", user.getPhone());
        request.setAttribute("defaultAddress", user.getAddress());

        return "customer/checkout"; // bạn sẽ làm HTML sau
    }

    @PostMapping("/checkout")
    public String doCheckout(
            @RequestParam String consignee,
            @RequestParam String phoneNumber,
            @RequestParam String deliveryAddress,
            @RequestParam String paymentMethod,
            @RequestParam(defaultValue = "0") Long deliveryFee,
            HttpServletRequest request
    ) {
        UserDTO user = (UserDTO) request.getSession().getAttribute("currentCustomer");
        if (user == null) return "redirect:/login";

        try {
            Long orderId = orderService.placeOrder(
                    user.getId(),
                    consignee,
                    phoneNumber,
                    deliveryAddress,
                    deliveryFee,
                    paymentMethod,
                    null,  // paymentStatus - handled by DAO
                    null   // deliveryStatus - handled by DAO
            );

            return "redirect:/my-orders/" + orderId; // sau khi đặt hàng → xem chi tiết
        } catch (Exception e) {
            // thường gặp: trigger báo vượt tồn kho
            request.setAttribute("error", e.getMessage());
            request.setAttribute("cartItems", cartService.getItemsByUser(user.getId()));
            return "customer/checkout";
        }
    }

    // ========== ORDER DETAIL ==========
    @GetMapping("/my-orders/{id}")
    public String orderDetail(@PathVariable("id") Long id, HttpServletRequest request) {
        UserDTO user = (UserDTO) request.getSession().getAttribute("currentCustomer");
        if (user == null) return "redirect:/login";

        OrderWeb order = (OrderWeb) orderService.getOrderDetail(id, user.getId());
        if (order == null) return "redirect:/"; // hoặc redirect:/my-orders

        request.setAttribute("order", order);
        return "customer/order_detail"; // bạn sẽ làm HTML sau
    }
}
