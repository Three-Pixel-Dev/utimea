package org.uit.utimea.features.timetable.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimetableGenerationRequest {
    private Long majorSectionId;
    private Long academicYearId;
}
