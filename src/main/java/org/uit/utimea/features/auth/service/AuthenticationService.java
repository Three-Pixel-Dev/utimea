package org.uit.utimea.features.auth.service;

import org.uit.utimea.features.auth.dto.request.ForgotPasswordRequest;
import org.uit.utimea.features.auth.dto.request.LoginRequest;
import org.uit.utimea.features.auth.dto.request.ResetPasswordRequest;
import org.uit.utimea.features.auth.dto.request.VerifyOtpRequest;
import org.uit.utimea.features.auth.dto.response.LoginResponse;

public interface AuthenticationService {
    LoginResponse login(LoginRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void verifyOtp(VerifyOtpRequest request);
    void resetPassword(ResetPasswordRequest request);
}
