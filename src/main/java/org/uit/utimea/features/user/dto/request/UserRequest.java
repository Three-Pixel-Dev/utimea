package org.uit.utimea.features.user.dto.request;

public record UserRequest(
        String email,
        String password,
        Long roleId
) {}
