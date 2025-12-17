package com.example.controller;

import com.example.dao.*;
import com.example.dto.ProductFormDTO;
import com.example.dto.UserDTO;
import com.example.model.*;
import com.example.service.NotificationService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.Locale.Category;

import javax.management.Notification;

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

    @Autowired
    private VoucherDao voucherDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private NotificationService notificationService;

    private static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm";

    private static final List<String> VALID_STATUSES = List.of(
            "Tất cả", "Chờ xác nhận", "Đang giao", "Hoàn thành", "Đã hủy"
    );
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
        Integer unreadCount = notificationService.getUnreadCount();
        List<Notification> notifications = notificationService.getRecentNotifications(5);

        request.setAttribute("revenue", revenue);
        request.setAttribute("newOrders", newOrders);
        request.setAttribute("totalCustomers", totalCustomers);
        request.setAttribute("totalProducts", totalProducts);
        request.setAttribute("recentOrders", recentOrders);
        request.setAttribute("unreadCount", unreadCount);
        request.setAttribute("notifications", notifications);

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

    @GetMapping("/admin/products/filter")
    public String filterProducts(HttpServletRequest request,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) Long brandId,
                                 @RequestParam(required = false) Long categoryId) {

        List<Product> products = adminProductDao.searchProducts(keyword, brandId, categoryId);
        request.setAttribute("products", products);

        // Trả về chỉ phần HTML bên trong div#product-list-container
        return "admin/products :: product-list-container";
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
    public String saveProduct(@ModelAttribute ProductFormDTO form) {
        try {
            Product product;
            if (form.getId() != null && form.getId() > 0) {
                // Load cũ lên
                product = adminProductDao.getProductById(form.getId());
                if (product.getProductImages() == null) product.setProductImages(new ArrayList<>());
                product.setUpdatedAt(LocalDateTime.now());
            } else {
                // THÊM MỚI
                product = new Product();
                product.setProductImages(new ArrayList<>());
                product.setCreatedAt(LocalDateTime.now());
                product.setUpdatedAt(LocalDateTime.now());
                product.setIsDelete(false);
            }

            // set thông tin từ dto
            product.setName(form.getName());
            product.setDescription(form.getDescription());
            product.setPrice(form.getPrice() != null ? form.getPrice() : 0L);
            product.setSalePrice(form.getSalePrice()); // Dòng này giờ đã hoạt động!
            product.setStatus(form.getStatus() != null ? form.getStatus() : "Đang bán");

            Long brandId = form.getBrandId();
            if (brandId != null) product.setBrand(new Brand(brandId));

            Long categoryId = form.getCategoryId();
            if (categoryId != null) product.setCategory(new Category(categoryId));

            // xử lý ảnh mới và xóa ảnh cũ
            MultipartFile imageFile = form.getImageFile();
            if (imageFile != null && !imageFile.isEmpty()) {

                // Logic xóa ảnh cũ
                String oldImagePath = form.getOldImagePath();
                if (product.getId() != null && oldImagePath != null && !oldImagePath.isEmpty()) {
                    ProductImage imageToRemove = null;
                    for (ProductImage oldImg : product.getProductImages()) {
                        if (oldImg.getPath().equals(oldImagePath)) {
                            imageToRemove = oldImg;
                            break;
                        }
                    }
                    if (imageToRemove != null) {
                        String oldFileName = oldImagePath.substring(oldImagePath.lastIndexOf('/') + 1);
                        Path oldPath = Paths.get(UPLOAD_DIR, oldFileName);
                        Files.deleteIfExists(oldPath);
                        product.getProductImages().remove(imageToRemove);
                    }
                }

                // Logic lưu file mới và tạo ProductImage mới
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

                String fileName = System.currentTimeMillis() + "_" + StringUtils.cleanPath(imageFile.getOriginalFilename());
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                ProductImage img = new ProductImage();
                img.setPath("/uploads/" + fileName);
                img.setIsPrimary(true);
                img.setCreatedAt(LocalDateTime.now());

                product.getProductImages().forEach(i -> i.setIsPrimary(false));
                product.addImage(img);
            }

            // Xử lys tồn kho và biến theer
            List<String> variantColors = form.getVariantColor();

            if (variantColors != null && !variantColors.isEmpty()) {
                product.setQuantity(0); // Set tổng Quantity về 0 nếu dùng Biến thể

            } else {
                product.setQuantity(form.getQuantity() != null ? form.getQuantity() : 0);

                // Nếu sản phẩm cũ có biến thể, cần xóa chúng nếu Admin bỏ check
                if (product.getVariants() != null) {

                }
            }

            boolean isSaved = adminProductDao.saveOrUpdate(product);

            if (isSaved) return "redirect:/admin/products?saveSuccess=true";
            else return "redirect:/admin/products?error=true";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/products?error=true";
        }
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
    public String listOrders(HttpServletRequest request,
                             @RequestParam(value = "status", required = false, defaultValue = "Tất cả") String status,
                             @RequestParam(value = "keyword", required = false) String keyword) {

        List<OrderWeb> orders;

        if ("Tất cả".equals(status) && (keyword == null || keyword.isEmpty()) ) {
            orders = orderDao.findFilteredOrders("Tất cả", null);
        } else {
            orders = orderDao.findFilteredOrders(status, keyword);
        }

        request.setAttribute("orders", orders);
        request.setAttribute("currentStatus", status);
        request.setAttribute("keyword", keyword);

        return "admin/order";
    }

    @GetMapping("/order/detail/{id}")
    public String orderDetail(@PathVariable("id") Long id, HttpServletRequest request) {
        OrderWeb order = orderDao.findById(id);

        if (order == null) {
            return "redirect:/admin/order";
        }

        request.setAttribute("order", order);

        return "admin/order_detail";
    }

    @PostMapping("/order/updateStatus")
    public String updateOrderStatus(@RequestParam("orderId") Long orderId,
                                    @RequestParam("newStatus") String newStatus,
                                    RedirectAttributes redirectAttributes) {

        // Danh sách các trạng thái hợp lệ
        List<String> VALID_STATUSES = List.of("Chờ xác nhận", "Đang giao", "Hoàn thành", "Đã hủy");

        // Kiểm tra trạng thái mới có hợp lệ không
        if (!VALID_STATUSES.contains(newStatus)) {
            redirectAttributes.addFlashAttribute("error", "Trạng thái cập nhật không hợp lệ.");
            return "redirect:/admin/order/detail/" + orderId;
        }

        try {
            orderDao.updateDeliveryStatus(orderId, newStatus);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái đơn hàng #" + orderId + " thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống khi cập nhật trạng thái.");
            e.printStackTrace();
        }

        return "redirect:/admin/order/detail/" + orderId;
    }

    @GetMapping("/customers")
    public String customerListPage(HttpServletRequest request) {
    	request.setAttribute("customers", userDao.getAllCustomer());
        return "admin/customer_list";
    }
    
    @GetMapping("/customer/{id}")
    public String customerDetail(@PathVariable("id") Long userId, HttpServletRequest request) {
    	request.setAttribute("info", userDao.getUserById(userId));
    	request.setAttribute("wishlist", userWishlistDao.getUserWishlistByUserId(userId));
    	request.setAttribute("cart", cartDao.getCartItemByUserId(userId));
    	request.setAttribute("orders", userDao.getAllOrderByUserId(userId));
    	return "admin/customer_detail";
    }
    
    @PostMapping("/delete/customer/{id}")
    @ResponseBody
    public Map<String, Object> deleteCustomer(@PathVariable("id") Long userId) {

        Map<String, Object> response = new HashMap<>();

        try {
            Long remainingUsers = userDao.deleteUserById(userId);

            if (remainingUsers > 0) {
                response.put("success", true);
                response.put("message", "Xóa khách hàng thành công");
                response.put("remainingUsers", remainingUsers);
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy khách hàng hoặc đã bị xóa");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi xóa khách hàng");
            e.printStackTrace();
        }

        return response;
    }

    @GetMapping("/vouchers")
    public String listVouchers(HttpServletRequest request) {
        List<Voucher> vouchers = voucherDao.findAll();

        request.setAttribute("vouchers", vouchers);
        request.setAttribute("voucher", new Voucher());

        return "admin/voucher";
    }

    @GetMapping("/vouchers/edit/{id}")
    public String editVoucher(@PathVariable("id") Long id, HttpServletRequest request) {
        Voucher voucher = voucherDao.findById(id);
        if (voucher == null) {
            return "redirect:/admin/vouchers?error=notfound";
        }

        request.setAttribute("voucher", voucher);
        request.setAttribute("vouchers", voucherDao.findAll()); // Hiển thị danh sách
        return "admin/voucher";
    }

    @PostMapping("/vouchers/save")
    public String saveVoucher(HttpServletRequest request, RedirectAttributes redirectAttributes) {

        String idParam = request.getParameter("id");
        Long id = (idParam != null && !idParam.isEmpty()) ? Long.parseLong(idParam) : null;

        Voucher voucher;
        if (id != null) {
            voucher = voucherDao.findById(id);
            if (voucher == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy Voucher cần cập nhật.");
                return "redirect:/admin/vouchers";
            }
        } else {
            voucher = new Voucher();
        }

        try {
            // --- Lấy các giá trị bắt buộc từ form ---
            String codeStr = request.getParameter("code");
            String discountAmountStr = request.getParameter("discountAmount");
            String minOrderAmountStr = request.getParameter("minOrderAmount");
            String quantityStr = request.getParameter("quantity");
            String expiryDateStr = request.getParameter("expiryDate");

            if (codeStr == null || codeStr.isEmpty() ||
                    discountAmountStr == null || discountAmountStr.isEmpty() ||
                    minOrderAmountStr == null || minOrderAmountStr.isEmpty() ||
                    quantityStr == null || quantityStr.isEmpty() ||
                    expiryDateStr == null || expiryDateStr.isEmpty()) { // KIỂM TRA EXPIRY DATE

                throw new IllegalArgumentException("Vui lòng điền đầy đủ Mã Code, Giá trị, Số lượng và Hạn sử dụng.");
            }

            voucher.setCode(codeStr);

            Long discountLong = Long.parseLong(request.getParameter("discount"));
            voucher.setQuantity(Integer.parseInt(quantityStr));

            try {
                voucher.setExpiryDate(LocalDateTime.parse(expiryDateStr));
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Định dạng Ngày/Giờ không hợp lệ. Vui lòng kiểm tra lại Hạn sử dụng.");
            }

            String isActiveParam = request.getParameter("isActive");
            voucher.setActive(isActiveParam != null);

            voucherDao.save(voucher);
            redirectAttributes.addFlashAttribute("success", "Lưu Voucher thành công!");

        } catch (NumberFormatException e) {
            // Xử lý lỗi khi parse Long/Integer thất bại
            redirectAttributes.addFlashAttribute("error", "Lỗi dữ liệu: Giá trị giảm, Đơn tối thiểu hoặc Số lượng phải là số nguyên hợp lệ.");
            e.printStackTrace();
            return "redirect:/admin/vouchers";
        } catch (IllegalArgumentException e) {
            // Xử lý lỗi nếu thiếu trường hoặc lỗi định dạng ngày giờ
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/admin/vouchers";
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("Duplicate entry")) {
                redirectAttributes.addFlashAttribute("error", "Mã Voucher đã tồn tại. Vui lòng chọn Mã Code khác.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống khi lưu Voucher.");
            }
            e.printStackTrace();
            return "redirect:/admin/vouchers";
        }

        return "redirect:/admin/vouchers";
    }

    @GetMapping("/vouchers/delete/{id}")
    public String deleteVoucher(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        voucherDao.delete(id);
        redirectAttributes.addFlashAttribute("success", "Xóa Voucher thành công!");
        return "redirect:/admin/vouchers";
    }

    @GetMapping("/notifications")
    public String listNotifications(HttpServletRequest request) {

        List<Notification> notifications = notificationService.findAll();

        request.setAttribute("notifications", notifications);

        return "admin/notifications";
    }

    @PostMapping("/notifications/mark-read/{id}")
    @ResponseBody
    public String markNotificationAsRead(@PathVariable Long id) {
        boolean success = notificationService.markAsRead(id);
        return success ? "OK" : "Error";
    }
}