package org.uit.utimea.features.dashboard.dto.response;

import lombok.Builder;

@Builder
public record DashboardResponse(
        Long totalRooms,
        Long totalTeachers,
        Long totalStudents
) {}
