package org.uit.utimea.features.role.dto.response;

import lombok.Builder;
import org.uit.utimea.shared.dto.response.MasterData;

@Builder
public record RoleResponse(
        Long id,
        String name,
        MasterData masterData
) {}
