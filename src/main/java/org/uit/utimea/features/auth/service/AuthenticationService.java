package org.uit.utimea.features.auth.service;

import org.uit.utimea.features.auth.dto.request.LoginRequest;
import org.uit.utimea.features.auth.dto.response.LoginResponse;

public interface AuthenticationService {
    LoginResponse login(LoginRequest request);
}
