package com.example.service;

import com.example.config.HibernateUtil;
import com.example.dao.UserDao;
import com.example.dto.UserDTO;
import com.example.model.OrderWeb;
import com.example.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class UserService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private JavaMailSender mailSender;

    public Long checkAccount(String email, String password) {
        return userDao.checkAccount(email, password);
    }

    public UserDTO findUserById(Long id) {
        User user = userDao.findUserById(id);
        return new UserDTO(user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getGender(),
                user.getRole());
    }

    public boolean checkEmail(String email) {
        return userDao.checkEmail(email);
    }

    public boolean checkPhone(String phone) {
        return userDao.checkPhone(phone);
    }

    public boolean save(User user) {
        return userDao.save(user);
    }

    public boolean vertify(String code, String codeVertify) {
        return code.equals(codeVertify);
    }

    public boolean checkPassword(String password1, String password2) {
        return password1.equals(password2);
    }

    public void changePassword(String emailChange, String password) {
        userDao.changePassword(emailChange,password);
    }

    public boolean updateUser(Long userId, String name, String phone, String address) {
        return userDao.updateUser(userId, name, phone, address);
    }

    public boolean checkOldPassword(Long userId, String oldPassword) {
        return userDao.checkOldPassword(userId, oldPassword);
    }

    public UserDTO getUserDtoById(Long id) {
        User user = userDao.findUserById(id);
        if (user == null) return null;

        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getGender(),
                user.getRole()
        );
    }

    public String generateOTP() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            otp.append("0123456789".charAt(random.nextInt(10)));
        }
        return otp.toString();
    }

    private String createEmailTemplate(String title, String bodyContent) {
        String year = String.valueOf(LocalDateTime.now().getYear());

        return "<!DOCTYPE html>"
                + "<html><head>"
                + "<meta charset='UTF-8'>"
                + "<style>"
                + "body { font-family: Helvetica, Arial, sans-serif; background-color: #fafafa; margin: 0; padding: 0; }"
                + ".btn { display: inline-block; background-color: #ff6600; color: #ffffff !important; padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: bold; margin-top: 20px; }"
                + "</style></head>"

                + "<body style='margin: 0; padding: 0; background-color: #fafafa; font-family: Helvetica, Arial, sans-serif;'>"

                + "<div style='width: 100%; background-color: #fafafa; padding: 30px 0;'>"

                + "<div class='container' style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; overflow: hidden; border: 1px solid #eee; box-shadow: 0 2px 8px rgba(0,0,0,0.05);'>"

                + "  <div class='header' style='background-color: #000000; padding: 25px; text-align: center;'>"
                + "    <a href='#' style='color: #ffffff; font-size: 26px; font-weight: bold; text-decoration: none; letter-spacing: 2px; font-family: sans-serif;'>"
                + "      B<span style='color: #ff6600;'>2</span>L SHOES"
                + "    </a>"
                + "  </div>"

                + "  <div class='content' style='padding: 40px 30px; color: #000000; line-height: 1.7; font-size: 16px;'>"
                + "    <h2 style='color: #000000; margin-top: 0; text-align: center; font-weight: bold;'>" + title + "</h2>"

                + "    <div style='color: #000000 !important;'>"
                +          bodyContent
                + "    </div>"

                + "  </div>"

                + "  <div class='footer' style='background-color: #f9f9f9; padding: 25px; text-align: center; font-size: 13px; color: #666666; border-top: 1px solid #eee;'>"
                + "    <p style='margin: 5px 0;'>Hotline: <a href='tel:1900xxxx' style='color: #ff6600; text-decoration: none; font-weight: bold;'>1900 xxxx</a> | Email: <a href='mailto:contact.b2lshoes@gmail.com' style='color: #ff6600; text-decoration: none; font-weight: bold;'>contact.b2lshoes@gmail.com</a></p>"
                + "    <p style='margin: 5px 0;'>&copy; " + year + " B2L Shoes. All rights reserved.</p>"
                + "  </div>"

                + "</div>"
                + "</div>"
                + "</body></html>";
    }
    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("contact.b2lshoes@gmail.com", "B2L Shoes");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async("mailExecutor")
    public void sendVerificationEmail(String email, String otpCode) {
        String body = "<p style='text-align: center; color: #000000;'>Xin chào quý khách,</p>"
                + "<p style='color: #000000;'>Chúng tôi nhận được yêu cầu xác thực cho tài khoản <b>" + email + "</b>.</p>"
                + "<p style='color: #000000;'>Mã xác minh (OTP) của bạn là:</p>"

                + "<div style='background-color: #fff3e0; color: #ff6600; font-size: 36px; font-weight: bold; text-align: center; padding: 20px; margin: 30px 0; letter-spacing: 8px; border-radius: 8px; border: 1px dashed #ff6600;'>"
                + otpCode
                + "</div>"

                + "<p style='text-align: center; color: #000000;'>Mã này có hiệu lực trong vòng <b>5 phút</b>. Vui lòng không chia sẻ cho bất kỳ ai.</p>";

        String htmlContent = createEmailTemplate("Xác thực tài khoản", body);
        sendHtmlEmail(email, "[B2L Shoes] Mã xác thực: " + otpCode, htmlContent);
    }

    @Async("mailExecutor")
    public void sendChangePasswordOtp(String email, String otpCode) {
        String body = "<p style='text-align: center; color: #000000;'>Xin chào,</p>"
                + "<p style='color: #000000;'>Hệ thống nhận được yêu cầu <b>đổi mật khẩu</b> cho tài khoản liên kết với email này.</p>"
                + "<p style='color: #000000;'>Đây là mã OTP xác thực của bạn:</p>"

                + "<div style='background-color: #fff3e0; color: #ff6600; font-size: 36px; font-weight: bold; text-align: center; padding: 20px; margin: 30px 0; letter-spacing: 8px; border-radius: 8px; border: 1px dashed #ff6600;'>"
                + otpCode
                + "</div>"

                + "<p style='text-align: center; color: #000000;'>Nếu bạn không thực hiện yêu cầu này, vui lòng đổi mật khẩu ngay lập tức.</p>";

        String htmlContent = createEmailTemplate("Yêu cầu đổi mật khẩu", body);
        sendHtmlEmail(email, "[B2L Shoes] Mã OTP đổi mật khẩu", htmlContent);
    }

    @Async("mailExecutor")
    public void sendWelcomeEmail(String email, String name) {
        String body = "<p>Xin chào <b>" + name + "</b>,</p>"
                + "<p>Chúc mừng bạn đã chính thức trở thành thành viên của <b>B2L Shoes</b>.</p>"
                + "<p>Hãy bắt đầu khám phá những mẫu giày thể thao mới nhất và tận hưởng ưu đãi dành riêng cho thành viên mới.</p>"
                + "<div style='text-align: center;'><a href='http://localhost:8080/products' class='btn'>MUA SẮM NGAY</a></div>";

        String htmlContent = createEmailTemplate("Chào mừng gia nhập!", body);
        sendHtmlEmail(email, "Chào mừng đến với B2L Shoes!", htmlContent);
    }

    @Async("mailExecutor")
    public void sendPasswordChangedNotification(String email, String name) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));
        String body = "<p>Xin chào <b>" + name + "</b>,</p>"
                + "<p>Mật khẩu tài khoản của bạn đã được thay đổi thành công vào lúc: <b>" + time + "</b>.</p>"
                + "<p style='color: red; margin-top: 20px;'>⚠️ Nếu bạn KHÔNG thực hiện thao tác này, vui lòng liên hệ ngay với chúng tôi để khóa tài khoản.</p>";

        String htmlContent = createEmailTemplate("Thay đổi mật khẩu thành công", body);
        sendHtmlEmail(email, "Thông báo bảo mật tài khoản", htmlContent);
    }
}
