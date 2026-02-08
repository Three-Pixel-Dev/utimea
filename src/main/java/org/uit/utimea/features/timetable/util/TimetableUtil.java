package org.uit.utimea.features.timetable.util;

import org.uit.utimea.shared.entity.TimetableInfo;

public class TimetableUtil {

    public static String generateTimetableName(TimetableInfo timetableInfo) {
        if (timetableInfo == null) {
            return "";
        }
        
        String academicYearName = timetableInfo.getAcademicYear() != null 
                ? timetableInfo.getAcademicYear().getName() 
                : "";
        String majorSectionName = timetableInfo.getMajorSection() != null 
                ? timetableInfo.getMajorSection().getName() 
                : "";
        
        if (academicYearName.isEmpty() && majorSectionName.isEmpty()) {
            return "";
        }
        
        if (academicYearName.isEmpty()) {
            return majorSectionName;
        }
        
        if (majorSectionName.isEmpty()) {
            return academicYearName;
        }
        
        return academicYearName + " - " + majorSectionName;
    }
}
