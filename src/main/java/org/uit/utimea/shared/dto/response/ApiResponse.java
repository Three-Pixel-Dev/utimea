package org.uit.utimea.shared.dto.response;

import lombok.Builder;

@Builder
public record ApiResponse(
        int success,
        int code,
        ApiMetaResponse meta,
        Object data,
        String message
) {}
