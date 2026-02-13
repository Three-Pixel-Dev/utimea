package org.uit.utimea.features.timetable.dto.request;

import java.time.LocalDate;

public record PeriodChangeRequest(
        Long timetableDataId,
        Long newTimetableDayId,
        Long newTimetablePeriodId,
        String requestScope, // SPECIFIC_DATE or PERMANENT
        LocalDate specificDate, // Required if requestScope is SPECIFIC_DATE
        String requestReason
) {}
