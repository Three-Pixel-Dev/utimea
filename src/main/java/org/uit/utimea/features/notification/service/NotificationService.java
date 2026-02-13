package org.uit.utimea.features.notification.service;

import org.uit.utimea.features.notification.dto.NotificationRequest;
import org.uit.utimea.features.notification.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {
    NotificationResponse createNotification(NotificationRequest request);
    NotificationResponse getNotificationById(String id);
    List<NotificationResponse> getNotificationsByTeacherId(Long teacherId);
    List<NotificationResponse> getNotificationsByTimetableInfoId(Long timetableInfoId);
    List<NotificationResponse> getNotificationsByMajorSectionId(Long majorSectionId);
    List<NotificationResponse> getAllNotifications();
}
