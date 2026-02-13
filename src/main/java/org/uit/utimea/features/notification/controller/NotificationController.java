package org.uit.utimea.features.notification.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uit.utimea.features.notification.dto.NotificationResponse;
import org.uit.utimea.features.notification.service.NotificationService;
import org.uit.utimea.shared.dto.response.ApiResponse;
import org.uit.utimea.shared.util.ApiResponseUtil;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<ApiResponse> getByTeacherId(
            @PathVariable Long teacherId,
            HttpServletRequest httpServletRequest) {
        List<NotificationResponse> notifications = notificationService.getNotificationsByTeacherId(teacherId);
        ApiResponse apiResponse = ApiResponseUtil.success(
                notifications,
                "Notifications retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/student/{majorSectionId}")
    public ResponseEntity<ApiResponse> getByMajorSectionId(
            @PathVariable Long majorSectionId,
            HttpServletRequest httpServletRequest) {
        List<NotificationResponse> notifications = notificationService.getNotificationsByMajorSectionId(majorSectionId);
        ApiResponse apiResponse = ApiResponseUtil.success(
                notifications,
                "Notifications retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }
}
