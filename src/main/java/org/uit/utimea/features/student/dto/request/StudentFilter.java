package org.uit.utimea.features.student.dto.request;

public record StudentFilter(
        String name,
        String phoneNumber,
        Long batchId,
        Long majorSectionId
) {}
