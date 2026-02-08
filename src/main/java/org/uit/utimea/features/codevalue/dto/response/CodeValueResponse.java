package org.uit.utimea.features.codevalue.dto.response;

import lombok.Builder;
import org.uit.utimea.shared.dto.response.MasterData;

@Builder
public record CodeValueResponse(
        Long id,
        Long codeId,
        String codeName,
        String codeValue,
        String description,
        Boolean systemDefined,
        MasterData masterData
) {}
