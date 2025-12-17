package com.example.controller;
import com.example.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer")
public class PaymentController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/payment/vnpay-callback")
    public String vnpayCallback(HttpServletRequest request, RedirectAttributes ra) {
        String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
        String orderIdStr = request.getParameter("vnp_TxnRef");

        // 1. Kiểm tra an toàn: Nếu tham số bị thiếu hoặc null
        if (orderIdStr == null || orderIdStr.equals("null") || orderIdStr.isEmpty()) {
            ra.addFlashAttribute("error", "Không tìm thấy thông tin đơn hàng từ cổng thanh toán.");
            return "redirect:/customer/orders";
        }

        try {
            Long orderId = Long.parseLong(orderIdStr);

            if ("00".equals(vnp_ResponseCode)) {
                // Thanh toán thành công
                orderService.updatePaymentStatus(orderId, "PAID");
                ra.addFlashAttribute("message", "Thanh toán thành công đơn hàng #" + orderId);
                return "redirect:/customer/orders/" + orderId;
            } else {
                // Thanh toán thất bại (người dùng hủy hoặc lỗi thẻ)
                ra.addFlashAttribute("error", "Giao dịch không thành công. Mã lỗi: " + vnp_ResponseCode);
                return "redirect:/customer/orders/" + orderId;
            }
        } catch (NumberFormatException e) {
            ra.addFlashAttribute("error", "Định dạng mã đơn hàng không hợp lệ.");
            return "redirect:/customer/orders";
        }
    }
}