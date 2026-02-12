package org.uit.utimea.features.timetable.dto.response;

import lombok.Builder;
import org.uit.utimea.shared.entity.Profile;

import java.util.List;

@Builder
public record TimetableDataResponse(
        Long id,
        CodeValueResponse timetableDay,
        CodeValueResponse timetablePeriod,
        SubjectResponse subject,
        RoomResponse room,
        String subjectType,
        TeacherResponse teacher
) {}
