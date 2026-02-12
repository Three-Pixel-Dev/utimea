package org.uit.utimea.features.timetable.dto.request;

public record TimetableFilter(
        Long majorSectionId,
        Long academicYearId,
        Long timetableDayId,
        Long timetablePeriodId,
        Long subjectId,
        Long roomId,
        Long teacherId
) {}
