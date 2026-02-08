package org.uit.utimea.features.subject.dto.response;

import lombok.Builder;

@Builder
public record CodeValueResponse(
        Long id,
        String name
) {}
