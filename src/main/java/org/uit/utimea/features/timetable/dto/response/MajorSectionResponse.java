package org.uit.utimea.features.timetable.dto.response;

import lombok.Builder;

@Builder
public record MajorSectionResponse(
        Long id,
        String name
) {}
