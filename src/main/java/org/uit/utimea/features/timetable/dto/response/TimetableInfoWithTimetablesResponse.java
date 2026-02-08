package org.uit.utimea.features.timetable.dto.response;

import lombok.Builder;
import org.uit.utimea.shared.dto.response.MasterData;

import java.util.List;

@Builder
public record TimetableInfoWithTimetablesResponse(
        Long id,
        String name,
        MajorSectionResponse majorSection,
        CodeValueResponse academicYear,
        MasterData masterData,
        List<TimetableResponse> timetables
) {}
