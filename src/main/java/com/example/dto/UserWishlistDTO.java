package com.example.dto;

import java.time.LocalDateTime;

import com.example.model.Product;
import com.example.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

public class UserWishlistDTO {
	private Long id;
	private Long userId;
	private Long productId;
	private String productName;
	private String productDescription;
	private Long productPrice;
	private String productBrand;
	private String productImagePath;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	public UserWishlistDTO() {
		
	}
	
	public UserWishlistDTO(Long id, Long userId, Long productId, String productName, String productDescription, Long productPrice, String productBrand,
			String productImagePath, LocalDateTime createdAt, LocalDateTime updatedAt) {
		this.id =  id;
		this.userId = userId;
		this.productId = productId;
		this.productName = productName;
		this.productDescription = productDescription;
		this.productPrice = productPrice;
		this.productBrand = productBrand;
		this.productImagePath = productImagePath;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getUserId() {
		return userId;
	}
	
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	public Long getProductId() {
		return productId;
	}
	
	public void setProductId(Long productId) {
		this.productId = productId;
	}
	
	public String getProductName() {
		return productName;
	}
	
	public void setProductName(String productName) {
		this.productName = productName;
	}
	
	public String getProductDescription() {
		return productDescription;
	}
	
	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}
	
	public Long getProductPrice() {
		return productPrice;
	}
	
	public void setProductPrice(Long productPrice) {
		this.productPrice = productPrice;
	}
	
	public String getProductBrand() {
		return productBrand;
	}
	
	public void setProductBrand(String productBrand) {
		this.productBrand = productBrand;
	}
	
	public String getProductImagePath() {
		return productImagePath;
	}
	 
	public void setProductImagePath(String productImagePath) {
		this.productImagePath = productImagePath;
	}
	
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	
	public LocalDateTime getUpdatedAt () {
		return updatedAt;
	}
	
	public void setUpdated(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}


}