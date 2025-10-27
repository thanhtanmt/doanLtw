package com.example.clothesshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String verificationCode) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Xác thực tài khoản Fashion Store");

        // ⚠️ Không dùng &lt; &gt; — dùng HTML thật
        String content = String.format("""
            <div style="font-family: Arial, sans-serif; padding: 20px; max-width: 600px; margin: auto;">
                <h2 style="color: #333;">Xác thực tài khoản Fashion Store</h2>
                <p>Cảm ơn bạn đã đăng ký tài khoản tại Fashion Store. Để hoàn tất quá trình đăng ký, vui lòng nhập mã xác thực sau:</p>
                <div style="background: #f5f5f5; padding: 15px; margin: 20px 0; text-align: center; font-size: 24px; letter-spacing: 5px;">
                    <strong>%s</strong>
                </div>
                <p>Mã xác thực này sẽ hết hạn sau 15 phút.</p>
                <p>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.</p>
            </div>
            """, verificationCode);

        helper.setText(content, true); // true = gửi dạng HTML
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String to, String verificationCode) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Đặt lại mật khẩu Fashion Store");

        String content = String.format("""
            <div style="font-family: Arial, sans-serif; padding: 20px; max-width: 600px; margin: auto;">
                <h2 style="color: #333;">Đặt lại mật khẩu Fashion Store</h2>
                <p>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản của mình. Vui lòng sử dụng mã xác thực sau:</p>
                <div style="background: #f5f5f5; padding: 15px; margin: 20px 0; text-align: center; font-size: 24px; letter-spacing: 5px;">
                    <strong>%s</strong>
                </div>
                <p>Mã xác thực này sẽ hết hạn sau 15 phút.</p>
                <p><strong>Lưu ý:</strong> Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này và đảm bảo tài khoản của bạn được bảo mật.</p>
            </div>
            """, verificationCode);

        helper.setText(content, true);
        mailSender.send(message);
    }
}
