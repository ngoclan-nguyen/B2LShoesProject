package com.example.controller;

import com.example.dto.CheckoutFormDTO;
import com.example.dto.CheckoutSummaryDTO;
import com.example.dto.UserDTO;
import com.example.model.OrderWeb;
import com.example.service.CheckoutService;
import com.example.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
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
            @RequestParam(value = "selectedIds", required = false) String selectedIdsString, // Nhận ID đã chọn
            Model model) {

        if (userDto == null) {
            return "redirect:/login";
        }

        if (selectedIdsString == null || selectedIdsString.isEmpty()) {
            return "redirect:/cart";
        }

        // Gọi Service với danh sách ID đã chọn
        CheckoutSummaryDTO summary = checkoutService.getCheckoutSummary(userDto.getId(), selectedIdsString);

        if (summary.getCartItems().isEmpty()) {
            // Trường hợp người dùng xóa cookie hoặc lỗi đồng bộ ID
            return "redirect:/cart";
        }

        model.addAttribute("summary", summary);
        model.addAttribute("customer", userDto);
        model.addAttribute("checkoutFormDTO", new CheckoutFormDTO());
        model.addAttribute("pageTitle", "Xác nhận Thanh toán");

        return "customer/checkout";
    }

    @PostMapping("/place-order")
    public String placeOrder(
            @SessionAttribute("currentCustomer") UserDTO userDto,
            @ModelAttribute CheckoutFormDTO form,
            RedirectAttributes redirectAttributes) {

        try {
            OrderWeb newOrder = orderService.placeOrder(userDto, form);

            redirectAttributes.addFlashAttribute("message", "Đặt hàng thành công! Vui lòng kiểm tra email.");
            return "redirect:/customer/orders/" + newOrder.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi đặt hàng: " + e.getMessage());

            return "redirect:/cart";
        }
    }

    @PostMapping("/checkout/selection")
    public String handleCheckoutSelection(
            @SessionAttribute(name = "currentCustomer", required = false) UserDTO userDto,
            @RequestParam("selectedVariantIds") String selectedVariantIds,
            RedirectAttributes redirectAttributes) {

        if (userDto == null) {
            return "redirect:/login";
        }

        if (selectedVariantIds == null || selectedVariantIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn sản phẩm để thanh toán.");
            return "redirect:/cart";
        }

        redirectAttributes.addAttribute("selectedIds", selectedVariantIds);

        return "redirect:/checkout";
    }
}