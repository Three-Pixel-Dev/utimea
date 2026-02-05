package org.uit.utimea.features.subject.dto.response;

import lombok.Builder;
import org.uit.utimea.shared.dto.response.MasterData;

@Builder
public record SubjectResponse(
        Long id,
        String code,
        String description,
        MasterData masterData
) {}
