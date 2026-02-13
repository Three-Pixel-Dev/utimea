package org.uit.utimea.features.notification.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        String id,
        String action,
        Long teacherId,
        Long timetableInfoId,
        Long majorSectionId,
        String readableText,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
