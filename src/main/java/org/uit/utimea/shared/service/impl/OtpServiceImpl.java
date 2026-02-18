package org.uit.utimea.shared.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements org.uit.utimea.shared.service.OtpService {

    private final StringRedisTemplate redisTemplate;
    private final Random random = new Random();

    @Value("${otp.expiration.minutes:10}")
    private int otpExpirationMinutes;

    @Value("${otp.length:6}")
    private int otpLength;

    @Override
    public String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    @Override
    public void storeOtp(String email, String otp) {
        String key = "otp:" + email;
        redisTemplate.opsForValue().set(key, otp, otpExpirationMinutes, TimeUnit.MINUTES);
    }

    @Override
    public boolean verifyOtp(String email, String otp) {
        String key = "otp:" + email;
        String storedOtp = redisTemplate.opsForValue().get(key);
        return storedOtp != null && storedOtp.equals(otp);
    }

    @Override
    public void deleteOtp(String email) {
        String key = "otp:" + email;
        redisTemplate.delete(key);
    }
}
