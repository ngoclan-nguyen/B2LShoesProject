package com.example.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.model.MasterSize;
import com.example.service.HomeService;
import com.example.service.MasterSizeService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("")
public class HomeController {
	@Autowired
    private HomeService homeService;
	
	@Autowired 
	private MasterSizeService masterSizeService;

    @GetMapping("/")
    public String home(HttpServletRequest request) {
        request.setAttribute("featuredProducts", homeService.getFeaturedProducts());
		request.setAttribute("bestSellerProducts", homeService.getBestSellerProducts());

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
		return "customer/terms_conditions";
	}

	@GetMapping("/privacy-policy")
	public String privacyPolicy() {
		return "customer/privacy_policy";
	}

	@GetMapping("/profile")
	public String profile(HttpServletRequest request) {return "customer/profile";}
}