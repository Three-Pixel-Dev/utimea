package org.uit.utimea.features.user.dto.response;

import lombok.Builder;

@Builder
public record RoleResponse(
        Long id,
        String name
) {}
