package org.uit.utimea.features.timetable.dto.response;

import lombok.Builder;

@Builder
public record SubjectResponse(
        Long id,
        String code,
        String description
) {}
