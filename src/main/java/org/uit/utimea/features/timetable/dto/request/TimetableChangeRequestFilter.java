package org.uit.utimea.features.timetable.dto.request;

public record TimetableChangeRequestFilter(
        Long timetableDataId,
        Long requestedById,
        String requestType, // ROOM_CHANGE or PERIOD_CHANGE
        String status, // PENDING, APPROVED, DECLINED
        Long processedById
) {}
