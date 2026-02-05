package org.uit.utimea.features.user.dto.response;

import lombok.Builder;
import org.uit.utimea.shared.dto.response.MasterData;

@Builder
public record UserResponse(
        Long id,
        String email,
        RoleResponse role,
        MasterData masterData
) {}
