package org.uit.utimea.features.codevalue.dto.response;

import lombok.Builder;

@Builder
public record CodeValueListResponse(
        Long id,
        Long codeId,
        String codeValue,
        String description
) {}
