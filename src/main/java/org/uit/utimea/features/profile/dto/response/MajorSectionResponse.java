package org.uit.utimea.features.profile.dto.response;

import lombok.Builder;

@Builder
public record MajorSectionResponse(
        Long id,
        String name
) {}
