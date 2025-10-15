package com.baohanh.trungtambaohanh.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Yêu cầu đặt lại mật khẩu";
        String resetUrl = "http://localhost:8080/reset-password?token=" + token;
        String message = "Để đặt lại mật khẩu, vui lòng nhấp vào liên kết dưới đây:\n" + resetUrl;

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(to);
        email.setSubject(subject);
        email.setText(message);
        mailSender.send(email);
    }
}