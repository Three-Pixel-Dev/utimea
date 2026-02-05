package org.uit.utimea.features.code.dto.response;

import lombok.Builder;
import org.uit.utimea.shared.dto.response.MasterData;

@Builder
public record CodeResponse(
        Long id,
        String name,
        String constantValue,
        MasterData masterData
) {}
