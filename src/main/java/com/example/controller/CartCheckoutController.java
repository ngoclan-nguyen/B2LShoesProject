package com.example.controller;
import com.example.dto.UserDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CartCheckoutController {

    @PostMapping("/checkout/selection")
    public String handleCheckoutSelection(
            @SessionAttribute(name = "currentCustomer", required = false) UserDTO userDto,
            @RequestParam("selectedVariantIds") String selectedVariantIds,
            @RequestParam(value = "appliedVoucherCode", required = false) String appliedVoucherCode,
            RedirectAttributes redirectAttributes) {

        if (userDto == null) {
            return "redirect:/login";
        }

        if (selectedVariantIds == null || selectedVariantIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn sản phẩm để thanh toán.");
            return "redirect:/cart";
        }

        redirectAttributes.addAttribute("selectedIds", selectedVariantIds);

        if (appliedVoucherCode != null && !appliedVoucherCode.isEmpty()) {
            redirectAttributes.addAttribute("voucherCode", appliedVoucherCode);
        }

        return "redirect:/customer/checkout";
    }
}