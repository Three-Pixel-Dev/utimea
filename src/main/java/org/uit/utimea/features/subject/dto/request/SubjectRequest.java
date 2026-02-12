package org.uit.utimea.features.subject.dto.request;

import java.util.List;

public record SubjectRequest(
        String code,
        String description,
        List<Long> subjectTypeIds,
        Long roomTypeId,
        List<Long> teacherIds
) {}
