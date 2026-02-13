package org.uit.utimea.features.timetable.dto.request;

import java.time.LocalDate;

public record RoomChangeRequest(
        Long timetableDataId,
        Long newRoomId,
        String requestScope, // SPECIFIC_DATE or PERMANENT
        LocalDate specificDate, // Required if requestScope is SPECIFIC_DATE
        String requestReason
) {}
