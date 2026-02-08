package org.uit.utimea.shared.repository;

import org.springframework.stereotype.Repository;
import org.uit.utimea.shared.entity.Timetable;

import java.util.List;

@Repository
public interface TimetableRepository extends BaseRepository<Timetable> {
    List<Timetable> findByTimetableInfo_Id(Long timetableInfoId);
}
