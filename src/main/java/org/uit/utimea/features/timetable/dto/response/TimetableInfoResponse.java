package org.uit.utimea.features.timetable.dto.response;

import lombok.Builder;

@Builder
public record TimetableInfoResponse(
        Long id,
        String name,
        MajorSectionResponse majorSection,
        CodeValueResponse academicYear
) {}
