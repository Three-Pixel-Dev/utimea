package org.uit.utimea.features.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uit.utimea.features.auth.dto.request.ForgotPasswordRequest;
import org.uit.utimea.features.auth.dto.request.LoginRequest;
import org.uit.utimea.features.auth.dto.request.ResetPasswordRequest;
import org.uit.utimea.features.auth.dto.request.VerifyOtpRequest;
import org.uit.utimea.features.auth.dto.response.LoginResponse;
import org.uit.utimea.features.auth.service.AuthenticationService;
import org.uit.utimea.shared.entity.User;
import org.uit.utimea.shared.repository.UserRepository;
import org.uit.utimea.shared.security.JwtTokenProvider;
import org.uit.utimea.shared.service.EmailService;
import org.uit.utimea.shared.service.OtpService;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        // Load user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        
        // Get user entity to extract role and ID
        User user = userRepository.findByEmailWithRole(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(userDetails, user.getRole().getName());

        return new LoginResponse(
                token,
                user.getEmail(),
                user.getRole().getName(),
                user.getId()
        );
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        // Check if user exists
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + request.email()));

        // Generate OTP
        String otp = otpService.generateOtp();

        // Store OTP in Redis
        otpService.storeOtp(request.email(), otp);

        // Send OTP via email
        emailService.sendOtpEmail(request.email(), otp);
    }

    @Override
    public void verifyOtp(VerifyOtpRequest request) {
        // Verify OTP
        boolean isValid = otpService.verifyOtp(request.email(), request.otp());
        if (!isValid) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // Verify OTP first
        boolean isValid = otpService.verifyOtp(request.email(), request.otp());
        if (!isValid) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }

        // Get user
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + request.email()));

        // Update password
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Delete OTP after successful password reset
        otpService.deleteOtp(request.email());
    }
}
