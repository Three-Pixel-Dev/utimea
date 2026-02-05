package org.uit.utimea.features.teacher.dto.request;

public record TeacherRequest(
        String name,
        String phoneNumber,
        String degree,
        Long departmentId
) {}
