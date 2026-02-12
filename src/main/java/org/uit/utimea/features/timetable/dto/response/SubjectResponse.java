package org.uit.utimea.features.timetable.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record SubjectResponse(
        Long id,
        String code,
        String description,
        List<TeacherResponse> teachers
) {}
