package org.uit.utimea.shared.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uit.utimea.shared.entity.TimetableData;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimetableDataRepository extends BaseRepository<TimetableData> {
    List<TimetableData> findByTimetables_TimetableInfo_AcademicYear_Id(Long academicYearId);
    
    // Check if teacher is free at a specific day and period (excluding specific timetable data IDs)
    @Query("SELECT COUNT(td) > 0 FROM TimetableData td " +
           "WHERE td.teacher.id = :teacherId " +
           "AND td.timetableDay.id = :dayId " +
           "AND td.timetablePeriod.id = :periodId " +
           "AND td.id NOT IN :excludeIds")
    boolean existsByTeacherAndDayAndPeriodExcluding(@Param("teacherId") Long teacherId, 
                                                     @Param("dayId") Long dayId, 
                                                     @Param("periodId") Long periodId,
                                                     @Param("excludeIds") List<Long> excludeIds);
    
    // Check if teacher is free at a specific day and period
    @Query("SELECT COUNT(td) > 0 FROM TimetableData td " +
           "WHERE td.teacher.id = :teacherId " +
           "AND td.timetableDay.id = :dayId " +
           "AND td.timetablePeriod.id = :periodId")
    boolean existsByTeacherAndDayAndPeriod(@Param("teacherId") Long teacherId, 
                                           @Param("dayId") Long dayId, 
                                           @Param("periodId") Long periodId);
    
    // Check if time slot (day + period + room) is already occupied (excluding specific timetable data IDs)
    @Query("SELECT COUNT(td) > 0 FROM TimetableData td " +
           "WHERE td.timetableDay.id = :dayId " +
           "AND td.timetablePeriod.id = :periodId " +
           "AND td.room.id = :roomId " +
           "AND td.id NOT IN :excludeIds")
    boolean existsByDayAndPeriodAndRoomExcluding(@Param("dayId") Long dayId, 
                                                  @Param("periodId") Long periodId, 
                                                  @Param("roomId") Long roomId,
                                                  @Param("excludeIds") List<Long> excludeIds);
    
    // Check if time slot (day + period + room) is already occupied
    @Query("SELECT COUNT(td) > 0 FROM TimetableData td " +
           "WHERE td.timetableDay.id = :dayId " +
           "AND td.timetablePeriod.id = :periodId " +
           "AND td.room.id = :roomId")
    boolean existsByDayAndPeriodAndRoom(@Param("dayId") Long dayId, 
                                       @Param("periodId") Long periodId, 
                                       @Param("roomId") Long roomId);
    
    // Check if same subject and teacher already exist on the same day (excluding specific timetable data IDs)
    @Query("SELECT COUNT(td) > 0 FROM TimetableData td " +
           "WHERE td.subject.id = :subjectId " +
           "AND td.teacher.id = :teacherId " +
           "AND td.timetableDay.id = :dayId " +
           "AND td.id NOT IN :excludeIds")
    boolean existsBySubjectAndTeacherAndDayExcluding(@Param("subjectId") Long subjectId,
                                                     @Param("teacherId") Long teacherId,
                                                     @Param("dayId") Long dayId,
                                                     @Param("excludeIds") List<Long> excludeIds);
    
    // Check if same subject and teacher already exist on the same day
    @Query("SELECT COUNT(td) > 0 FROM TimetableData td " +
           "WHERE td.subject.id = :subjectId " +
           "AND td.teacher.id = :teacherId " +
           "AND td.timetableDay.id = :dayId")
    boolean existsBySubjectAndTeacherAndDay(@Param("subjectId") Long subjectId,
                                            @Param("teacherId") Long teacherId,
                                            @Param("dayId") Long dayId);
}
