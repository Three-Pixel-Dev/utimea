package org.uit.utimea.features.timetable.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.uit.utimea.shared.data.Sem;


@Getter
@Setter
public class TimetableGenerationRequest {
    private Long academicYearId;
    private Long numberOfStudentsInFirstYear;
    private Long numberOfStudentsInSecondYear;
    private Sem sem;
}
