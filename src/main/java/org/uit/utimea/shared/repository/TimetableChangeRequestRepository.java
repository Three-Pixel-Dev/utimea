package org.uit.utimea.shared.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uit.utimea.shared.entity.TimetableChangeRequest;

import java.util.List;

@Repository
public interface TimetableChangeRequestRepository extends BaseRepository<TimetableChangeRequest> {
    
    @Query("SELECT tcr FROM TimetableChangeRequest tcr " +
           "WHERE tcr.timetableData.id = :timetableDataId " +
           "AND tcr.status = org.uit.utimea.shared.entity.TimetableChangeRequest$RequestStatus.PENDING")
    List<TimetableChangeRequest> findPendingByTimetableDataId(@Param("timetableDataId") Long timetableDataId);
    
    @Query("SELECT tcr FROM TimetableChangeRequest tcr " +
           "WHERE tcr.requestedBy.id = :teacherId " +
           "ORDER BY tcr.requestedAt DESC")
    List<TimetableChangeRequest> findByRequestedById(@Param("teacherId") Long teacherId);
    
    @Query("SELECT tcr FROM TimetableChangeRequest tcr " +
           "WHERE tcr.status = org.uit.utimea.shared.entity.TimetableChangeRequest$RequestStatus.APPROVED " +
           "AND tcr.requestScope = org.uit.utimea.shared.entity.TimetableChangeRequest$RequestScope.SPECIFIC_DATE " +
           "AND tcr.specificDate IS NOT NULL " +
           "AND tcr.specificDate < :currentDate")
    List<TimetableChangeRequest> findApprovedSpecificDateRequestsBeforeDate(@Param("currentDate") java.time.LocalDate currentDate);
}
