package org.uit.utimea.features.profile.dto.response;

import lombok.Builder;
import org.uit.utimea.shared.dto.response.MasterData;

@Builder
public record ProfileResponse(
        Long id,
        String name,
        String phoneNumber,
        String degree,
        CodeValueResponse department,
        CodeValueResponse batch,
        MajorSectionResponse majorSection,
        MasterData masterData
) {}
