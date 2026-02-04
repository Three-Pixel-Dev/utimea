package org.uit.utimea.shared.dto.response;

import lombok.Builder;

@Builder
public record ApiMetaResponse(
        String endpoint,
        String method,
        int totalItems, // only for pagination
        int totalPages, // only for pagination
        int currentPage // only for pagination
) {}
