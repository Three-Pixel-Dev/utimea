package org.uit.utimea.features.majorsection.dto.response;

import lombok.Builder;
import org.uit.utimea.shared.dto.response.MasterData;

@Builder
public record MajorSectionResponse(
        Long id,
        String name,
        CodeValueResponse majorSectionYear,
        MasterData masterData
) {}
