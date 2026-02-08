package org.uit.utimea.shared.repository;

import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uit.utimea.shared.entity.Timetable;

import java.util.List;

@Repository
public interface TimetableRepository extends BaseRepository<Timetable> {
    List<Timetable> findByTimetableInfo_Id(Long timetableInfoId);
    
    @Query("SELECT DISTINCT t FROM Timetable t " +
           "LEFT JOIN FETCH t.timetableInfo ti " +
           "LEFT JOIN FETCH ti.majorSection " +
           "LEFT JOIN FETCH ti.academicYear " +
           "LEFT JOIN FETCH t.timetableData td " +
           "LEFT JOIN FETCH td.timetableDay " +
           "LEFT JOIN FETCH td.timetablePeriod " +
           "LEFT JOIN FETCH td.subject " +
           "LEFT JOIN FETCH td.room " +
           "WHERE t.timetableInfo.id = :timetableInfoId")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<Timetable> findByTimetableInfo_IdWithAllRelations(@Param("timetableInfoId") Long timetableInfoId);
}
