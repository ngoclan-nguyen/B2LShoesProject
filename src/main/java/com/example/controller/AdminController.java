package com.example.controller;

import com.example.dao.AdminProductDao;
import com.example.dao.DashboardDao;
import com.example.dao.ProductDao;
import com.example.dao.UserDao;
import com.example.dto.UserDTO;
import com.example.model.*;
import com.example.service.RememberMeService;
import com.example.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private static final String UPLOAD_DIR = "uploads";
    @Autowired
    private UserDao userDao;

    @Autowired
    private RememberMeService rememberMeService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private DashboardDao dashboardDao;

    @Autowired
    private AdminProductDao adminProductDao;

    @GetMapping("/login")
    public String loginPage(HttpServletRequest request) {
        if (request.getSession().getAttribute("currentAdmin") != null) {
            return "redirect:/admin/dashboard";
        }
        return "admin/login";
    }

    @GetMapping("/account")
    public String account(HttpServletRequest request) {
        if(request.getSession().getAttribute("currentAdmin") == null) {
            return "redirect:/admin/login";
        }
        return "admin/404";
    }

    @PostMapping("/loginProcess")
    public String loginProcess(HttpServletRequest request, HttpServletResponse response) {
        String email = request.getParameter("username");
        String password = request.getParameter("password");
        String remember = request.getParameter("remember-me");

        Long id = userDao.checkAccount(email, password);
        if (id == -1L) return "redirect:/admin/login?error=email_not_found";
        if (id == -2L) return "redirect:/admin/login?error=wrong_password";

        // Lấy User và check Role
        User user = userDao.findUserById(id);
        String role = user.getRole();

        if (role == null || (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("ROLE_ADMIN"))) {
            return "redirect:/admin/login?error=access_denied";
        }

        UserDTO userDto = userService.getUserDtoById(id);
        request.getSession().setAttribute("currentAdmin", user);

        if ("on".equals(remember)) {
            String token = UUID.randomUUID().toString();

            RememberMeToken rmt = new RememberMeToken();
            rmt.setToken(token);
            rmt.setExpiryDate(LocalDateTime.now().plusDays(7));

            // Lưu dùng ID của DTO
            rememberMeService.save(rmt, userDto.getId());

            Cookie cookie = new Cookie("remember-me-admin", token);
            cookie.setMaxAge(7 * 24 * 60 * 60);
            cookie.setPath("/admin");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        session.removeAttribute("currentAdmin");
        session.invalidate();

        // Xóa Cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("remember-me-admin".equals(c.getName())) {
                    rememberMeService.removeToken(c.getValue());
                    Cookie newCookie = new Cookie("remember-me-admin", null);
                    newCookie.setMaxAge(0);
                    newCookie.setPath("/admin");
                    response.addCookie(newCookie);
                }
            }
        }
        return "redirect:/admin/login";
    }


    @PostMapping("/forget-pass-view")
    public String showForgotPasswordForm(HttpServletRequest request) {
        request.setAttribute("viewState", "INPUT_EMAIL");
        return "admin/login";
    }

    @PostMapping("/forget-pass")
    public String sendOtp(HttpServletRequest request) throws MessagingException {
        String email = request.getParameter("email");
        HttpSession session = request.getSession();

        User user = userDao.findByEmail(email);

        if (user == null) {
            request.setAttribute("alert", "Email không tồn tại!");
            request.setAttribute("viewState", "INPUT_EMAIL");
            return "admin/login";
        }

        String role = user.getRole();
        if (role == null || !role.equalsIgnoreCase("ADMIN")) {
            request.setAttribute("alert", "Không có quyền Admin!");
            return "redirect:/admin/login";
        }

        String otp = userService.generateOTP();
        session.setAttribute("RESET_EMAIL", email);
        session.setAttribute("RESET_OTP", otp);

        userService.sendChangePasswordOtp(email, otp);

        request.setAttribute("success", "Đã gửi mã OTP!");
        request.setAttribute("viewState", "INPUT_OTP");
        return "admin/login";
    }

    @PostMapping("/verify-code")
    public String verifyCode(HttpServletRequest request) {
        String codeUser = request.getParameter("code");
        HttpSession session = request.getSession();
        String codeServer = (String) session.getAttribute("RESET_OTP");

        if (codeServer != null && codeServer.equals(codeUser)) {
            session.removeAttribute("RESET_OTP");
            request.setAttribute("viewState", "INPUT_NEW_PASS");
            return "admin/login";
        }

        request.setAttribute("error", "Mã xác nhận sai!");
        request.setAttribute("viewState", "INPUT_OTP");
        return "admin/login";
    }

    @PostMapping("/change-password")
    public String changePass(HttpServletRequest request) {
        String newPass = request.getParameter("newPass");
        String confirmPass = request.getParameter("confirmPass");
        HttpSession session = request.getSession();

        if (newPass == null || newPass.length() < 6) {
            request.setAttribute("alert", "Mật khẩu phải có ít nhất 6 ký tự!");
            request.setAttribute("viewState", "INPUT_NEW_PASS");
            return "admin/login";
        }

        String regex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,}$";
        if (!newPass.matches(regex)) {
            request.setAttribute("alert", "Mật khẩu phải chứa cả chữ và số!");
            request.setAttribute("viewState", "INPUT_NEW_PASS");
            return "admin/login";
        }

        if (!newPass.equals(confirmPass)) {
            request.setAttribute("alert", "Mật khẩu xác nhận không trùng khớp!");
            request.setAttribute("viewState", "INPUT_NEW_PASS");
            return "admin/login";
        }

        String email = (String) session.getAttribute("RESET_EMAIL");
        if (email != null) {
            userService.changePassword(email, newPass);
            session.removeAttribute("RESET_EMAIL");
            return "redirect:/admin/login?success=changed";
        }
        return "redirect:/admin/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request) {
        Long revenue = dashboardDao.getTotalRevenue();
        Long newOrders = dashboardDao.getNewOrdersCount();
        Long totalCustomers = dashboardDao.getTotalCustomers();
        Long totalProducts = dashboardDao.getTotalProducts();

        List<OrderWeb> recentOrders = dashboardDao.getRecentOrders();

        request.setAttribute("revenue", revenue);
        request.setAttribute("newOrders", newOrders);
        request.setAttribute("totalCustomers", totalCustomers);
        request.setAttribute("totalProducts", totalProducts);
        request.setAttribute("recentOrders", recentOrders);

        return "admin/dashboard";
    }

    @GetMapping("/products")
    public String listPage(HttpServletRequest request) {
        String keyword = request.getParameter("keyword");
        String brandStr = request.getParameter("brandId");
        String cateStr = request.getParameter("categoryId");

        Long brandId = (brandStr != null && !brandStr.isEmpty()) ? Long.valueOf(brandStr) : null;
        Long categoryId = (cateStr != null && !cateStr.isEmpty()) ? Long.valueOf(cateStr) : null;

        List<Product> products = adminProductDao.searchProducts(keyword, brandId, categoryId);

        List<Brand> brands = adminProductDao.getAllBrands();
        List<Category> categories = adminProductDao.getAllCategories();

        request.setAttribute("products", products);
        request.setAttribute("totalProducts", products.size());

        request.setAttribute("brands", brands);
        request.setAttribute("categories", categories);

        request.setAttribute("keyword", keyword);
        request.setAttribute("currentBrandId", brandId);
        request.setAttribute("currentCategoryId", categoryId);
        return "admin/products";
    }

    @GetMapping("product/edit/{id}")
    public String editPage(@PathVariable Long id, HttpServletRequest request) {
        Product product = adminProductDao.getProductById(id);
        if (product == null) {
            return "redirect:/admin/products";
        }

        request.setAttribute("p", product);
        request.setAttribute("brands", adminProductDao.getAllBrands());
        request.setAttribute("categories", adminProductDao.getAllCategories());

        return "admin/edit";
    }

    @PostMapping("product/save")
    public String saveProduct(HttpServletRequest request,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        try {
            // 1. LẤY ID & KHỞI TẠO PRODUCT
            Long id = safeParseLong(request.getParameter("id"));
            Product product;

            if (id != null && id > 0) {
                // Sửa: Load cũ lên
                product = adminProductDao.getProductById(id);
                // Fix lỗi null list
                if (product.getProductImages() == null) {
                    product.setProductImages(new ArrayList<>());
                }
                product.setUpdatedAt(LocalDateTime.now());
            } else {
                // Thêm mới
                product = new Product();
                product.setProductImages(new ArrayList<>());
                product.setCreatedAt(LocalDateTime.now());
                product.setUpdatedAt(LocalDateTime.now());
                product.setIsDelete(false);
                product.setStatus("Active"); // Mặc định Active
            }

            // 2. SET THÔNG TIN CƠ BẢN
            product.setName(request.getParameter("name"));
            product.setPrice(safeParseLong(request.getParameter("price")) != null ? safeParseLong(request.getParameter("price")) : 0L);
            product.setQuantity(safeParseInt(request.getParameter("quantity")) != null ? safeParseInt(request.getParameter("quantity")) : 0);
            product.setDescription(request.getParameter("description"));

            Long brandId = safeParseLong(request.getParameter("brandId"));
            if (brandId != null) { Brand b = new Brand(); b.setId(brandId); product.setBrand(b); }

            Long categoryId = safeParseLong(request.getParameter("categoryId"));
            if (categoryId != null) { Category c = new Category(); c.setId(categoryId); product.setCategory(c); }

            // Xử lý Status (nếu form có gửi lên)
            String status = request.getParameter("status");
            if(status != null) product.setStatus(status);

            // ======================================================
            // 3. XỬ LÝ FILE ẢNH (CHỈ CHẠY KHI CÓ ẢNH UPLOAD)
            // ======================================================
            if (imageFile != null && !imageFile.isEmpty()) {

                // Trỏ vào thư mục uploads ở gốc dự án (Giống logic Review)
                Path uploadPath = Paths.get(UPLOAD_DIR);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String fileName = System.currentTimeMillis() + "_" + StringUtils.cleanPath(imageFile.getOriginalFilename());
                Path filePath = uploadPath.resolve(fileName);

                // Lưu file vật lý
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Tạo đối tượng ảnh
                ProductImage img = new ProductImage();
                img.setPath("/uploads/" + fileName); // Đường dẫn chuẩn
                img.setSize((int) imageFile.getSize());
                img.setIsPrimary(true);
                img.setCreatedAt(LocalDateTime.now());

                // Reset ảnh chính cũ (nếu có)
                if (product.getId() != null && product.getProductImages() != null) {
                    for (ProductImage oldImg : product.getProductImages()) {
                        oldImg.setIsPrimary(false);
                    }
                }

                // Thêm vào list
                product.addImage(img);
            }
            // ======================================================

            // 4. LƯU VÀO DATABASE (QUAN TRỌNG: PHẢI ĐỂ Ở NGOÀI CÙNG)
            // Code cũ của bạn để dòng này trong khối if(image) nên không lưu được nếu không có ảnh
            boolean isSaved = adminProductDao.saveOrUpdate(product);

            if (isSaved) return "redirect:/admin/products?saveSuccess=true";
            else return "redirect:/admin/products?error=true";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/products?error=true";
        }
    }

    private Long safeParseLong(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try { return Long.valueOf(value); } catch (Exception e) { return null; }
    }

    private Integer safeParseInt(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try { return Integer.valueOf(value); } catch (Exception e) { return null; }
    }

    @GetMapping("/product/add")
    public String addProductPage(HttpServletRequest request) {
        request.setAttribute("p", new Product());
        request.setAttribute("brands", adminProductDao.getAllBrands());
        request.setAttribute("categories", adminProductDao.getAllCategories());

        return "admin/add_product";
    }

    @GetMapping("product/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        boolean result = adminProductDao.deleteProduct(id);
        if (result) {
            return "redirect:/admin/products?deleteSuccess=true";
        }
        return "redirect:/admin/products?error=true";
    }

    @GetMapping("/orders")
    public String orderListPage(HttpServletRequest request) {

        return "admin/order";
    }

    @GetMapping("/customers")
    public String customerListPage(HttpServletRequest request) {
        return "admin/customer_list";
    }

}