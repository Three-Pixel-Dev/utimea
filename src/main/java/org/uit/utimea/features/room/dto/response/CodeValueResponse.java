package org.uit.utimea.features.room.dto.response;

import lombok.Builder;

@Builder
public record CodeValueResponse(
        Long id,
        String name
) {}
