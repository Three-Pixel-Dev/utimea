package org.uit.utimea.features.timetable.dto.response;

import lombok.Builder;

@Builder
public record TeacherResponse(
        Long id,
        String name,
        String phoneNumber,
        String degree
) {}
