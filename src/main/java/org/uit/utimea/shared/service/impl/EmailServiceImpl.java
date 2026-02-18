package org.uit.utimea.shared.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.uit.utimea.shared.service.EmailService;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset OTP - Utimea");
        message.setText(
            "Dear User,\n\n" +
            "You have requested to reset your password. Please use the following OTP to verify your identity:\n\n" +
            "OTP: " + otp + "\n\n" +
            "This OTP will expire in 10 minutes.\n\n" +
            "If you did not request this password reset, please ignore this email.\n\n" +
            "Best regards,\n" +
            "Utimea Team"
        );
        mailSender.send(message);
    }
}
