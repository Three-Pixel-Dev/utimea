package org.uit.utimea.features.notification.dto;

public record NotificationRequest(
        String action,
        Long teacherId,
        Long timetableInfoId,
        Long majorSectionId
) {}
