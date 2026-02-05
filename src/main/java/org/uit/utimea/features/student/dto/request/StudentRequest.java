package org.uit.utimea.features.student.dto.request;

public record StudentRequest(
        String name,
        String phoneNumber,
        Long batchId,
        Long majorSectionId
) {}
