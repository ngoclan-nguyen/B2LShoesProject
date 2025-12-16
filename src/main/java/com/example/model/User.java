package com.example.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

import com.example.model.CartItem;
import com.example.model.OrderWeb;
import com.example.model.ProductReview;
import com.example.model.ProductView;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "name", length = 200)
    private String name;

    @Column(name = "email", length = 200)
    private String email;

    @Column(name = "gender")
    private String gender; // True: Nam, False: Nữ

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "is_delete")
    private Boolean isDeleted = false; // Mặc định là false

    @Column(name = "password", length = 200)
    private String password;

    @Column(name = "title", length = 45)
    private String title;

    @Column(name = "role", length = 45)
    private String role;

    // 1. Đơn hàng (Mapped by 'customer' trong OrderWeb)
    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "customer",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
    private List<OrderWeb> orderWebs;

    // 2. Danh sách yêu thích (Mapped by 'user' trong UserWishlist)
    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "user",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
    private List<UserWishlist> userWishLists;

    // 3. Lịch sử xem (Mapped by 'customer' trong ProductView)
    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "customer",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
    private List<ProductView> productViews;

    // 4. Giỏ hàng (Mapped by 'user' trong CartItem)
    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "user",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
    private List<CartItem> cartItems;

    // 5. Đánh giá sản phẩm (Mapped by 'customer' trong ProductReview)
    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "customer",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
    private List<ProductReview> productReviews;

    // 6. Token ghi nhớ đăng nhập (Mapped by 'user' trong RememberMeToken)
    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "user",
            cascade = CascadeType.ALL)
    private List<RememberMeToken> tokens;

    public User() {
    }
    public User(Long id){

    }
    public User(Long id, LocalDateTime createdAt, LocalDateTime updatedAt, String address, String name, String email, String gender, String phone, Boolean isDeleted, String password, String title) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.address = address;
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.phone = phone;
        this.isDeleted = isDeleted;
        this.password = password;
        this.title = title;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRole() {return role;}

    public void setRole(String role) {this.role = role;}

    public List<OrderWeb> getOrderWebs() {
        return orderWebs;
    }

    public void setOrderWebs(List<OrderWeb> orderWebs) {
        this.orderWebs = orderWebs;
    }

    public List<UserWishlist> getUserWishLists() {
        return userWishLists;
    }

    public void setUserWishLists(List<UserWishlist> userWishLists) {
        this.userWishLists = userWishLists;
    }

    public List<ProductView> getProductViews() {
        return productViews;
    }

    public void setProductViews(List<ProductView> productViews) {
        this.productViews = productViews;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public List<ProductReview> getProductReviews() {
        return productReviews;
    }

    public void setProductReviews(List<ProductReview> productReviews) {
        this.productReviews = productReviews;
    }

    public List<RememberMeToken> getTokens() {
        return tokens;
    }

    public void setTokens(List<RememberMeToken> tokens) {
        this.tokens = tokens;
    }
}