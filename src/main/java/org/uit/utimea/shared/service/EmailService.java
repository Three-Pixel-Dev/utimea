package org.uit.utimea.shared.service;

public interface EmailService {
    void sendOtpEmail(String to, String otp);
}
