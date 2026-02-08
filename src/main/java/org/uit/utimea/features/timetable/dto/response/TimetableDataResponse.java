package org.uit.utimea.features.timetable.dto.response;

import lombok.Builder;

@Builder
public record TimetableDataResponse(
        Long id,
        CodeValueResponse timetableDay,
        CodeValueResponse timetablePeriod,
        SubjectResponse subject,
        RoomResponse room
) {}
