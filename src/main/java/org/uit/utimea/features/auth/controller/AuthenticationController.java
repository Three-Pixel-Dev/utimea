package org.uit.utimea.features.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uit.utimea.features.auth.dto.request.ForgotPasswordRequest;
import org.uit.utimea.features.auth.dto.request.LoginRequest;
import org.uit.utimea.features.auth.dto.request.ResetPasswordRequest;
import org.uit.utimea.features.auth.dto.request.VerifyOtpRequest;
import org.uit.utimea.features.auth.dto.response.LoginResponse;
import org.uit.utimea.features.auth.service.AuthenticationService;
import org.uit.utimea.shared.dto.response.ApiResponse;
import org.uit.utimea.shared.util.ApiResponseUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        LoginResponse response = authenticationService.login(request);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Login successful",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpServletRequest
    ) {
        authenticationService.forgotPassword(request);
        ApiResponse apiResponse = ApiResponseUtil.success(
                null,
                "OTP has been sent to your email",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            HttpServletRequest httpServletRequest
    ) {
        authenticationService.verifyOtp(request);
        ApiResponse apiResponse = ApiResponseUtil.success(
                null,
                "OTP verified successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletRequest httpServletRequest
    ) {
        authenticationService.resetPassword(request);
        ApiResponse apiResponse = ApiResponseUtil.success(
                null,
                "Password reset successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }
}
