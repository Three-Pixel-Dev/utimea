package org.uit.utimea.features.teacher.dto.request;

public record TeacherFilter(
        String name,
        String phoneNumber,
        String degree,
        Long departmentId
) {}
