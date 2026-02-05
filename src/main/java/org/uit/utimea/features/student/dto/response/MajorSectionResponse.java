package org.uit.utimea.features.student.dto.response;

import lombok.Builder;

@Builder
public record MajorSectionResponse(
        Long id,
        String name
) {}
