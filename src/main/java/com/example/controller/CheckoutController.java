package com.example.controller;

import com.example.dto.CheckoutFormDTO;
import com.example.dto.CheckoutSummaryDTO;
import com.example.dto.PaymentLinkRequestDTO;
import com.example.dto.UserDTO;
import com.example.model.Consignee;
import com.example.model.OrderWeb;
import com.example.service.CheckoutService;
import com.example.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer")
public class CheckoutController {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/checkout")
    public String showCheckoutPage(
            @SessionAttribute(name = "currentCustomer", required = false) UserDTO userDto,
            @RequestParam(value = "selectedIds", required = false) String selectedIdsString,
            @RequestParam(value = "voucherCode", required = false) String voucherCode,
            Model model) {

        // 1. Kiểm tra Đăng nhập
        if (userDto == null) {
            // Chuyển hướng đến trang đăng nhập nếu chưa đăng nhập
            return "redirect:/login";
        }

        // 2. Kiểm tra Sản phẩm đã chọn
        if (selectedIdsString == null || selectedIdsString.isEmpty()) {
            // Chuyển hướng về giỏ hàng nếu không có sản phẩm nào được chọn
            return "redirect:/cart";
        }

        // 3. Lấy Tóm tắt Thanh toán
        CheckoutSummaryDTO summary = checkoutService.getCheckoutSummary(
                userDto.getId(),
                selectedIdsString,
                voucherCode);

        // 4. Kiểm tra Giỏ hàng có hợp lệ
        if (summary == null || summary.getCartItems().isEmpty()) {
            // Lỗi đồng bộ ID hoặc giỏ hàng trống không hợp lệ
            return "redirect:/cart";
        }

        // 5. Chuẩn bị Model chính cho Form (Nếu chưa có)
        // Tên form chính thức trong Thymeleaf sẽ là "checkoutForm"
        if (!model.containsAttribute("checkoutForm")) {
            // Khởi tạo form lần đầu
            CheckoutFormDTO checkoutForm = new CheckoutFormDTO();

            // 5a. Chuẩn bị Consignee (Người nhận hàng)
            // Bạn đã tạo Model Consignee, bây giờ tạo đối tượng và đặt vào Form
            checkoutForm.setConsignee(new Consignee());

            model.addAttribute("checkoutForm", checkoutForm);
        }
        // Ghi chú: Nếu đã có lỗi từ POST request trước, Spring sẽ giữ lại 'checkoutForm' trong model.

        // 6. Thêm các thuộc tính khác vào Model
        model.addAttribute("summary", summary);
        model.addAttribute("customer", userDto);
        model.addAttribute("pageTitle", "Xác nhận Thanh toán");

        // 7. Trả về View
        return "customer/checkout";
    }
}