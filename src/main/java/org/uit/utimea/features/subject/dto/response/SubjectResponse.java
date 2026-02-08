package org.uit.utimea.features.subject.dto.response;

import lombok.Builder;
import org.uit.utimea.shared.dto.response.MasterData;

import java.util.List;

@Builder
public record SubjectResponse(
        Long id,
        String code,
        String description,
        List<CodeValueResponse> subjectTypes,
        CodeValueResponse roomType,
        MasterData masterData
) {}
