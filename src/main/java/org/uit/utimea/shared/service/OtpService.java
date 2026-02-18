package org.uit.utimea.shared.service;

public interface OtpService {
    String generateOtp();
    void storeOtp(String email, String otp);
    boolean verifyOtp(String email, String otp);
    void deleteOtp(String email);
}
