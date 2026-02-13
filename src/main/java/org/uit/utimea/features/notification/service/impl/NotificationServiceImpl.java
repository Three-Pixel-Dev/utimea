package org.uit.utimea.features.notification.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.uit.utimea.features.notification.dto.NotificationRequest;
import org.uit.utimea.features.notification.dto.NotificationResponse;
import org.uit.utimea.features.notification.service.NotificationService;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private final RestClient notificationRestClient;

    public NotificationServiceImpl(@Qualifier("notificationRestClient") RestClient notificationRestClient) {
        this.notificationRestClient = notificationRestClient;
    }

    @Override
    public NotificationResponse createNotification(NotificationRequest request) {
        try {
            log.info("Creating notification: action={}, teacherId={}, timetableInfoId={}, majorSectionId={}",
                    request.action(), request.teacherId(), request.timetableInfoId(), request.majorSectionId());

            NotificationResponse response = notificationRestClient.post()
                    .uri("/api/notifications")
                    .body(request)
                    .retrieve()
                    .body(NotificationResponse.class);

            log.info("Notification created successfully with id: {}", response != null ? response.id() : "unknown");
            return response;
        } catch (Exception e) {
            log.error("Error creating notification", e);
            throw new RuntimeException("Failed to create notification: " + e.getMessage(), e);
        }
    }

    @Override
    public NotificationResponse getNotificationById(String id) {
        try {
            log.debug("Fetching notification with id: {}", id);
            return notificationRestClient.get()
                    .uri("/api/notifications/{id}", id)
                    .retrieve()
                    .body(NotificationResponse.class);
        } catch (Exception e) {
            log.error("Error fetching notification with id: {}", id, e);
            throw new RuntimeException("Failed to fetch notification: " + e.getMessage(), e);
        }
    }

    @Override
    public List<NotificationResponse> getNotificationsByTeacherId(Long teacherId) {
        try {
            log.debug("Fetching notifications for teacherId: {}", teacherId);
            NotificationResponse[] response = notificationRestClient.get()
                    .uri("/api/notifications/teacher/{teacherId}", teacherId)
                    .retrieve()
                    .body(NotificationResponse[].class);
            return response != null ? Arrays.asList(response) : List.of();
        } catch (Exception e) {
            log.error("Error fetching notifications for teacherId: {}", teacherId, e);
            throw new RuntimeException("Failed to fetch notifications: " + e.getMessage(), e);
        }
    }

    @Override
    public List<NotificationResponse> getNotificationsByTimetableInfoId(Long timetableInfoId) {
        try {
            log.debug("Fetching notifications for timetableInfoId: {}", timetableInfoId);
            NotificationResponse[] response = notificationRestClient.get()
                    .uri("/api/notifications/timetable-info/{timetableInfoId}", timetableInfoId)
                    .retrieve()
                    .body(NotificationResponse[].class);
            return response != null ? Arrays.asList(response) : List.of();
        } catch (Exception e) {
            log.error("Error fetching notifications for timetableInfoId: {}", timetableInfoId, e);
            throw new RuntimeException("Failed to fetch notifications: " + e.getMessage(), e);
        }
    }

    @Override
    public List<NotificationResponse> getNotificationsByMajorSectionId(Long majorSectionId) {
        try {
            log.debug("Fetching notifications for majorSectionId: {}", majorSectionId);
            NotificationResponse[] response = notificationRestClient.get()
                    .uri("/api/notifications/major-section/{majorSectionId}", majorSectionId)
                    .retrieve()
                    .body(NotificationResponse[].class);
            return response != null ? Arrays.asList(response) : List.of();
        } catch (Exception e) {
            log.error("Error fetching notifications for majorSectionId: {}", majorSectionId, e);
            throw new RuntimeException("Failed to fetch notifications: " + e.getMessage(), e);
        }
    }

    @Override
    public List<NotificationResponse> getAllNotifications() {
        try {
            log.debug("Fetching all notifications");
            NotificationResponse[] response = notificationRestClient.get()
                    .uri("/api/notifications")
                    .retrieve()
                    .body(NotificationResponse[].class);
            return response != null ? Arrays.asList(response) : List.of();
        } catch (Exception e) {
            log.error("Error fetching all notifications", e);
            throw new RuntimeException("Failed to fetch notifications: " + e.getMessage(), e);
        }
    }
}
