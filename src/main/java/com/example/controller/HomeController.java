package com.example.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.service.HomeService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/customer")
public class HomeController {
	@Autowired
    private HomeService homeService;

    @GetMapping("/")
    public String home(HttpServletRequest request) {
        request.setAttribute("featuredProducts", homeService.getFeaturedProducts());
		request.setAttribute("bestSellerProducts", homeService.getBestSellerProducts());

        return "customer/home.html"; 
    }

	@GetMapping("/login")
	public String login(HttpServletRequest request) {
		return "customer/login.html";
	}

	@GetMapping("/register")
	public String register(HttpServletRequest request) {
		return "customer/register.html";
	}

	@GetMapping("/shopping-guide")
	public String shoppingGuide(HttpServletRequest request) {
		return "customer/shopping_guide.html";
	}

	@GetMapping("/size-guide")
	public String shoeSizeGuide(HttpServletRequest request) {
		return "customer/shoesize_guide.html";
	}

	@GetMapping("/warranty-policy")
	public String warrantyPolicy(HttpServletRequest request) {
		return "customer/warranty_policy.html";
	}

	@GetMapping("/about-us")
	public String aboutUs(HttpServletRequest request) {
		return "customer/about_us.html";
	}

	@GetMapping("/terms-conditions")
	public String termsConditions() {
		return "customer/terms_conditions";
	}
}