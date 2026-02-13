package org.uit.utimea.features.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.uit.utimea.features.auth.dto.request.LoginRequest;
import org.uit.utimea.features.auth.dto.response.LoginResponse;
import org.uit.utimea.features.auth.service.AuthenticationService;
import org.uit.utimea.shared.entity.User;
import org.uit.utimea.shared.repository.UserRepository;
import org.uit.utimea.shared.security.JwtTokenProvider;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

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
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(userDetails, user.getRole().getName());

        return new LoginResponse(
                token,
                user.getEmail(),
                user.getRole().getName(),
                user.getId()
        );
    }
}
