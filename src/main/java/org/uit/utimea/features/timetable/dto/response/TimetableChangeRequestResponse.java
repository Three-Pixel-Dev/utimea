package org.uit.utimea.features.timetable.dto.response;

import lombok.Builder;
import org.uit.utimea.shared.dto.response.MasterData;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record TimetableChangeRequestResponse(
        Long id,
        TimetableDataResponse timetableData,
        String requestType, // ROOM_CHANGE or PERIOD_CHANGE
        String requestScope, // SPECIFIC_DATE or PERMANENT
        String status, // PENDING, APPROVED, DECLINED, COMPLETED
        RoomResponse newRoom, // For room change
        CodeValueResponse newTimetableDay, // For period change
        CodeValueResponse newTimetablePeriod, // For period change
        LocalDate specificDate,
        TeacherResponse requestedBy,
        UserResponse processedBy,
        LocalDateTime processedAt,
        String requestReason,
        String adminComment,
        LocalDateTime requestedAt,
        MasterData masterData
) {
    @Builder
    public record RoomResponse(
            Long id,
            String name,
            Integer capacity
    ) {}

    @Builder
    public record CodeValueResponse(
            Long id,
            String name
    ) {}

    @Builder
    public record TeacherResponse(
            Long id,
            String name,
            String phoneNumber,
            String degree
    ) {}

    @Builder
    public record UserResponse(
            Long id,
            String email
    ) {}
}
