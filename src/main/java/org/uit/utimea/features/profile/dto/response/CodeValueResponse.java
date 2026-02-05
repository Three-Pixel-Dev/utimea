package org.uit.utimea.features.profile.dto.response;

import lombok.Builder;

@Builder
public record CodeValueResponse(
        Long id,
        String name
) {}
