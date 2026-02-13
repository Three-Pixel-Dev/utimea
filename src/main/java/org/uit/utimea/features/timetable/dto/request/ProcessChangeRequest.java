package org.uit.utimea.features.timetable.dto.request;

public record ProcessChangeRequest(
        Long requestId,
        String action, // APPROVE or DECLINE
        String adminComment
) {}
