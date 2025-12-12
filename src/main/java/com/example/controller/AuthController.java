package com.example.controller;

import com.example.dto.UserDTO;
import com.example.model.RememberMeToken;
import com.example.model.User;
import com.example.service.RememberMeService;
import com.example.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.mail.MessagingException;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    RememberMeService rememberMeService;

    private static final long OTP_RESEND_COOLDOWN = 60 * 1000; // 60 giây chờ gửi lại
    private static final long OTP_EXPIRATION_TIME = 5 * 60 * 1000; // 5 phút hết hạn mã

    @GetMapping("/login")
    public String login(@RequestParam(name = "redirect", required = false) String redirect, HttpServletRequest request) {
            if (redirect != null) {
                request.setAttribute("redirectUrl", redirect);
            }
            return "customer/login";
        }

    @PostMapping("/loginProcess")
    public String loginProcess(@RequestParam("email") String email,
                               @RequestParam("password") String password,
                               @RequestParam(value = "remember", required = false) String remember,
                               @RequestParam(value = "redirect", required = false) String redirectUrl,
                               HttpServletRequest request, HttpServletResponse response) {
        Long id = userService.checkAccount(email, password);

        if (id <= 0) {
            if (id == -1) {
                request.setAttribute("alert", "Tài khoản chưa tồn tại (Email không đúng)!");
            } else if (id == -2) {
                request.setAttribute("alert", "Mật khẩu không đúng, vui lòng thử lại!");
            } else {
                request.setAttribute("alert", "Đăng nhập thất bại, vui lòng thử lại sau!");
            }


            request.setAttribute("email", email);
            if (redirectUrl != null) {
                request.setAttribute("redirectUrl", redirectUrl);
            }

            return "customer/login.html";
        }

        UserDTO userDto = userService.getUserDtoById(id);
        request.getSession().setAttribute("currentCustomer", userDto);

        if ("on".equals(remember)) {
            String token = UUID.randomUUID().toString();

            RememberMeToken rmt = new RememberMeToken();
            rmt.setToken(token);
            rmt.setExpiryDate(LocalDateTime.now().plusDays(7));
            rememberMeService.save(rmt, userDto.getId());

            Cookie cookie = new Cookie("remember-me", token);
            cookie.setMaxAge(7 * 24 * 60 * 60);
            cookie.setPath("/customer");
            response.addCookie(cookie);
        }
        // Nếu có link redirect (ví dụ: /product/123) thì quay lại đó
        if (redirectUrl != null && !redirectUrl.trim().isEmpty()) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/";
    }

    @GetMapping("/loginProcess")
    public String loginProcessGet() {
        return "redirect:/login";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(HttpServletRequest request) {
        request.setAttribute("viewState", "INPUT_EMAIL");
        return "customer/login.html";
    }

    @PostMapping("/forgetPass")
    public String forgetPass(@RequestParam("email") String email, HttpServletRequest request) throws MessagingException {
        HttpSession session = request.getSession();

        Long lastSentTime = (Long) session.getAttribute("otpLastSentTime");
        long currentTime = System.currentTimeMillis();

        if (lastSentTime != null && (currentTime - lastSentTime < OTP_RESEND_COOLDOWN)) {
            long waitSeconds = (OTP_RESEND_COOLDOWN - (currentTime - lastSentTime)) / 1000;
            request.setAttribute("alert", "Vui lòng chờ " + waitSeconds + " giây trước khi gửi lại mã!");
            request.setAttribute("viewState", "INPUT_EMAIL");
            request.setAttribute("email", email);
            return "customer/login.html";
        }

        boolean checkEmail = userService.checkEmail(email);
        if(!checkEmail) {
            request.setAttribute("alert", "Email này chưa đăng ký trên hệ thống!");
            request.setAttribute("viewState", "INPUT_EMAIL");
            return "customer/login.html";
        }

        String otp = userService.generateOTP();

        session.setAttribute("verifyCodeServer", otp);
        session.setAttribute("emailChange", email);
        session.setAttribute("otpCreationTime", System.currentTimeMillis());
        session.setAttribute("verificationType", "FORGOT_PASS"); // Đánh dấu là đang Quên MK

        userService.sendChangePasswordOtp(email, otp);

        request.setAttribute("success", "Mã xác minh đang được gửi vào email!");
        request.setAttribute("viewState", "INPUT_OTP");
        try {
            Thread.sleep(1200); // delay backend
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "customer/login.html";
    }

    @PostMapping("/vertifyCode")
    public String vertifyCode(@RequestParam("codeUser") String codeUser, HttpServletRequest request) {
        HttpSession session = request.getSession();
        String codeServer = (String) session.getAttribute("verifyCodeServer");
        String type = (String) session.getAttribute("verificationType"); // Lấy loại hành động
        Long creationTime = (Long) session.getAttribute("otpCreationTime");

        if (codeServer == null || creationTime == null) {
            request.setAttribute("alert", "Phiên làm việc hết hạn!");
            return "redirect:/login";
        }
        if (System.currentTimeMillis() - creationTime > 5 * 60 * 1000) {
            request.setAttribute("alert", "Mã OTP đã hết hạn.");
            return "redirect:/login";
        }

        if (!codeUser.equals(codeServer)) {
            request.setAttribute("alert", "Mã xác minh không đúng!");
            request.setAttribute("viewState", "INPUT_OTP");
            return "customer/login.html";
        }

        // Trường hợp 1: ĐĂNG KÝ
        if ("REGISTER".equals(type)) {
            User tempUser = (User) session.getAttribute("tempUser");
            if(tempUser != null) {
                userService.save(tempUser);
                userService.sendWelcomeEmail(tempUser.getEmail(), tempUser.getName());

                session.removeAttribute("tempUser");
                session.removeAttribute("verifyCodeServer");
                session.removeAttribute("verificationType");

                request.setAttribute("success", "Đăng ký thành công! Hãy đăng nhập ngay.");
                return "customer/login.html";
            }
        }

        // Trường hợp 2: QUÊN MẬT KHẨU
        else if ("FORGOT_PASS".equals(type)) {
            session.removeAttribute("verifyCodeServer");
            // Chuyển sang màn hình nhập mật khẩu mới
            request.setAttribute("viewState", "INPUT_NEW_PASS");
            return "customer/login.html";
        }

        return "redirect:/login";
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestParam("password") String password,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpServletRequest request) {

        if(!password.equals(confirmPassword)) {
            request.setAttribute("alert", "Mật khẩu xác nhận không trùng khớp!");
            request.setAttribute("viewState", "INPUT_NEW_PASS");
            return "customer/login.html";
        }

        HttpSession session = request.getSession();
        String email = (String) session.getAttribute("emailChange");

        if(email == null) {
            request.setAttribute("alert", "Phiên làm việc hết hạn, vui lòng thực hiện lại!");
            return "redirect:/forgot-password";
        }

        Long checkId = userService.checkAccount(email, password);

        if (checkId > 0) {
            request.setAttribute("alert", "Mật khẩu mới không được trùng với mật khẩu cũ!");
            request.setAttribute("viewState", "INPUT_NEW_PASS");
            return "customer/login.html";
        }

        userService.changePassword(email, password);

        session.removeAttribute("emailChange");
        session.removeAttribute("otpLastSentTime");
        session.removeAttribute("otpCreationTime");

        userService.sendPasswordChangedNotification(email, "Khách hàng");
        try {
            Thread.sleep(1200); // delay backend
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        request.setAttribute("success", "Đổi mật khẩu thành công! Hãy đăng nhập lại.");
        return "customer/login.html";
    }

    @GetMapping("/change-password")
    public String changePasswordPage(HttpServletRequest request) {
        if (request.getSession().getAttribute("currentCustomer") == null) {
            return "redirect:/login";
        }
        return "customer/change-password.html";
    }

    // Dành cho đổi mật khẩu ở dropdown
    @PostMapping("/change-password-process")
    public String changePasswordProcess(@RequestParam("oldPassword") String oldPassword,
                                        @RequestParam("newPassword") String newPassword,
                                        @RequestParam("confirmPassword") String confirmPassword,
                                        HttpServletRequest request) {

        UserDTO currentUser = (UserDTO) request.getSession().getAttribute("currentCustomer");
        if (currentUser == null) return "redirect:/login";

        boolean isOldPassCorrect = userService.checkOldPassword(currentUser.getId(), oldPassword);
        if (!isOldPassCorrect) {
            request.setAttribute("alert", "Mật khẩu hiện tại không đúng!");
            return "customer/change-password.html";
        }

        if (newPassword.equals(oldPassword)) {
            request.setAttribute("alert", "Mật khẩu mới không được trùng với mật khẩu hiện tại!");
            return "customer/change-password.html";
        }

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("alert", "Mật khẩu xác nhận không trùng khớp!");
            return "customer/change-password.html";
        }

        userService.changePassword(currentUser.getEmail(), newPassword);

        userService.sendPasswordChangedNotification(currentUser.getEmail(), currentUser.getName());
        try {
            Thread.sleep(1200); // delay backend
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        request.setAttribute("success", "Đổi mật khẩu thành công!");

        return "customer/change-password.html";
    }

    private void reloadUserForView(HttpServletRequest request, Long userId) {

        UserDTO user = userService.findUserById(userId);
        request.setAttribute("user", user);
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        session.setAttribute("currentCustomer", null);
        session.invalidate();

        Cookie[] cookies = request.getCookies();
        for(Cookie cookie: cookies) {
            if(cookie.getName().equals("remember-me")) {
                String token = cookie.getValue();
                rememberMeService.removeToken(token);
            }
        }

        Cookie cookie = new Cookie("remember-me", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return "redirect:/";
    }

    @GetMapping("/register")
    public String register(HttpServletRequest request) {

        return "customer/register.html";
    }

    @PostMapping("/registerProcess")
    public String registerProcess(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam(value = "gender", defaultValue = "Nam") String gender,
            @RequestParam(value = "address", defaultValue = "") String address,
            HttpServletRequest request) {

        if (!password.equals(confirmPassword)) {
            request.setAttribute("alert", "Mật khẩu nhập lại không khớp!");
            request.setAttribute("name", name);
            request.setAttribute("email", email);
            request.setAttribute("phone", phone);
            return "customer/register.html";
        }


        if (userService.checkEmail(email)) {
            request.setAttribute("alert", "Email này đã được sử dụng!");
            request.setAttribute("name", name);
            request.setAttribute("phone", phone);
            return "customer/register.html";
        }
        String phonePattern = "^0\\d{9}$";
        if (!phone.matches(phonePattern)) {
            request.setAttribute("alert", "Số điện thoại không hợp lệ!");
            request.setAttribute("name", name);
            request.setAttribute("email", email);
            return "customer/register.html";
        }

        if (userService.checkPhone(phone)) {
            request.setAttribute("alert", "Số điện thoại này đã được sử dụng!");
            request.setAttribute("name", name);
            request.setAttribute("email", email);
            return "customer/register.html";
        }

        // Tạo User nhưng chưa lưu vào db
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPhone(phone);
        newUser.setPassword(password);
        newUser.setGender(gender);
        newUser.setAddress(address);
        newUser.setRole("Customer");
        newUser.setIsDeleted(false);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        String otp = userService.generateOTP();

        // Lưu tạm thông tin vào Session
        HttpSession session = request.getSession();
        session.setAttribute("tempUser", newUser);
        session.setAttribute("verifyCodeServer", otp);
        session.setAttribute("otpCreationTime", System.currentTimeMillis());
        session.setAttribute("verificationType", "REGISTER"); // Đánh dấu là đang Đăng Ký

        userService.sendVerificationEmail(email, otp);

        request.setAttribute("success", "Đăng ký gần xong! Nhập mã OTP trong email để hoàn tất.");
        request.setAttribute("viewState", "INPUT_OTP");

        return "customer/login.html";
    }
}