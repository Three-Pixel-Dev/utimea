package org.uit.utimea.features.teacher.dto.response;

import lombok.Builder;
import org.uit.utimea.shared.dto.response.MasterData;

@Builder
public record TeacherResponse(
        Long id,
        String name,
        String phoneNumber,
        String degree,
        CodeValueResponse department,
        MasterData masterData
) {}
