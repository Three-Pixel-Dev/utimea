package org.uit.utimea.features.student.dto.request;

public record StudentRequest(
        String name,
        String phoneNumber,
        String email,
        Long batchId,
        Long majorSectionId
) {}
