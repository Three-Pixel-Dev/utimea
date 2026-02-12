package org.uit.utimea.features.subject.dto.response;

import lombok.Builder;
import org.uit.utimea.shared.dto.response.MasterData;
import org.uit.utimea.features.teacher.dto.response.TeacherResponse;

import java.util.List;

@Builder
public record SubjectResponse(
        Long id,
        String code,
        String description,
        List<CodeValueResponse> subjectTypes,
        CodeValueResponse roomType,
        List<TeacherResponse> teachers,
        MasterData masterData
) {}
