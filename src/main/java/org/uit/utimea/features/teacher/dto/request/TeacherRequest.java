package org.uit.utimea.features.teacher.dto.request;

public record TeacherRequest(
        String name,
        String phoneNumber,
        String email,
        String degree,
        Long departmentId
) {}
