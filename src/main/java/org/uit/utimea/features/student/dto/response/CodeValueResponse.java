package org.uit.utimea.features.student.dto.response;

import lombok.Builder;

@Builder
public record CodeValueResponse(
        Long id,
        String name
) {}
