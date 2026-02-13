package org.uit.utimea.features.auth.dto.response;

public record LoginResponse(
        String token,
        String email,
        String role,
        Long userId
) {
}
