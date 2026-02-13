package org.uit.utimea.features.student.dto.response;

import lombok.Builder;
import org.uit.utimea.shared.dto.response.MasterData;

@Builder
public record StudentResponse(
        Long id,
        String name,
        String phoneNumber,
        String email,
        CodeValueResponse batch,
        MajorSectionResponse majorSection,
        MasterData masterData
) {}
