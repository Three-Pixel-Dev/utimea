package org.uit.utimea.features.profile.dto.request;

public record ProfileRequest(
        String name,
        String phoneNumber,
        String degree,
        Long departmentId,
        Long batchId,
        Long majorSectionId
) {}
