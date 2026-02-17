package org.uit.utimea.shared.dto.request;

import java.util.List;

public record BulkDeleteRequest(
        List<Long> ids
) {}
