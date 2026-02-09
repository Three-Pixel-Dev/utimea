package org.uit.utimea.shared.repository;

import org.springframework.stereotype.Repository;
import org.uit.utimea.shared.entity.TimetableData;

import java.util.List;

@Repository
public interface TimetableDataRepository extends BaseRepository<TimetableData> {
    List<TimetableData> findByTimetables_TimetableInfo_AcademicYear_Id(Long academicYearId);
}
