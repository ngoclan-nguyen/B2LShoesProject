package com.example.controller;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.dto.UserDTO;
import com.example.model.MasterSize;
import com.example.service.CartService;
import com.example.service.HomeService;
import com.example.service.MasterSizeService;
import com.example.service.UserWishlistService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("")
public class HomeController {
	@Autowired
    private HomeService homeService;
	
	@Autowired 
	private MasterSizeService masterSizeService;
	
	@Autowired 
	private UserWishlistService userWishlistService;
	
	@Autowired
	private CartService cartService;

    @GetMapping("/")
    public String home(HttpServletRequest request) {
        request.setAttribute("featuredProducts", homeService.getFeaturedProducts());
		request.setAttribute("bestSellerProducts", homeService.getBestSellerProducts());
		
		UserDTO user = (UserDTO) request.getSession().getAttribute("currentCustomer");

	    List<Long> userWishlistProductIds;
	    if (user != null) {
	        // Nếu user đã đăng nhập, lấy danh sách productId trong wishlist
	        userWishlistProductIds = userWishlistService.getUserWishlistProductIds(user.getId());
	    } else {
	        // Nếu chưa đăng nhập, trả về danh sách rỗng
	        userWishlistProductIds = List.of();
	    }

	    request.setAttribute("userWishlistProductIds", userWishlistProductIds);

        return "customer/home";
    }

	@GetMapping("/shopping-guide")
	public String shoppingGuide(HttpServletRequest request) {
		return "customer/shopping_guide";
	}

	@GetMapping("/size-guide")
	public String shoeSizeGuide(HttpServletRequest request) {
		request.setAttribute("allSize", masterSizeService.getAllSize());
		return "customer/shoesize_guide";
	}

	@GetMapping("/warranty-policy")
	public String warrantyPolicy(HttpServletRequest request) {
		return "customer/warranty_policy";
	}

	@GetMapping("/about-us")
	public String aboutUs(HttpServletRequest request) {
		return "customer/about_us";
	}

	@GetMapping("/terms-conditions")
	public String termsConditions() {
		return "customer/terms_conditions.html";
	}

	@GetMapping("/privacy-policy")
	public String privacyPolicy() {
		return "customer/privacy_policy.html";
	}
	
	@GetMapping("/wishlist")
	public String wishlist(HttpServletRequest request) {
		UserDTO user = (UserDTO)request.getSession().getAttribute("currentCustomer");
		Long userId = null;
		if (user != null)
			userId = user.getId();
		request.setAttribute("userWishlist", userWishlistService.getUserWishlistByUserId(userId));
		return "customer/wishlist.html";
	}
	
	@GetMapping("/cart")
	public String cart(HttpServletRequest request) {
		UserDTO user = (UserDTO)request.getSession().getAttribute("currentCustomer");
		Long userId = null;
		if (user != null)
			userId = user.getId();
		request.setAttribute("cartItem", cartService.getCartItemByUserId(userId));
		return "customer/shopping_cart";
	}

	@GetMapping("/profile")
	public String profile(HttpServletRequest request) {return "customer/profile";}
}