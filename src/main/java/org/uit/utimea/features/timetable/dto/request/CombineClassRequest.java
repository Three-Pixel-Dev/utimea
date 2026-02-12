package org.uit.utimea.features.timetable.dto.request;

public record CombineClassRequest(
        Long period1Id,  // Timetable ID from section 1
        Long period2Id,  // Timetable ID from section 2
        Long combineDayId,  // Day ID for the combined class
        Long combinePeriodId,  // Period ID for the combined class
        Long roomId,  // Room ID for the combined class
        Long teacherId  // Teacher ID for the combined class
) {}
