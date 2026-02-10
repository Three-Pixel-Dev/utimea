package org.uit.utimea.features.timetable.dto.request;

import lombok.Data;
import java.util.Map;

@Data
public class TimetableGenerationRequest {

    private Long academicYearId;

    private String sem;

    private Long numberOfStudentsInFirstYear;
    private Long numberOfStudentsInSecondYear;

    private Long numberOfStudentInThirdYear;
    private Long numberOfStudentInFourthYear;

    // --- SEMESTER 2 SPECIFIC ---
    // Example: { "SE": 45, "KE": 20, "HPC": 15, "CSec": 30 ... }
    private Map<String, Integer> thirdYearMajorCounts;
    private Map<String, Integer> fourthYearMajorCounts;
}
