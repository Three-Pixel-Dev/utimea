package org.uit.utimea.features.timetable.dto.response;

import lombok.Builder;

@Builder
public record CodeValueResponse(
        Long id,
        String name
) {}
