package org.uit.utimea.features.timetable.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uit.utimea.features.timetable.service.TimetableChangeRequestSchedulerService;
import org.uit.utimea.shared.entity.TimetableChangeRequest;
import org.uit.utimea.shared.entity.TimetableData;
import org.uit.utimea.shared.repository.TimetableChangeRequestRepository;
import org.uit.utimea.shared.repository.TimetableDataRepository;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimetableChangeRequestSchedulerServiceImpl implements TimetableChangeRequestSchedulerService {

    private final TimetableChangeRequestRepository repository;
    private final TimetableDataRepository timetableDataRepository;

    /**
     * Runs every Sunday at 2:00 AM to revert expired SPECIFIC_DATE timetable change requests
     * Cron expression: second, minute, hour, day of month, month, day of week
     * 0 0 2 * * 0 = Every Sunday at 2:00 AM (0 = Sunday)
     */
    @Scheduled(cron = "0 0 2 * * 0")
    @Transactional
    public void revertExpiredSpecificDateRequests() {
        log.info("Starting scheduled task to revert expired SPECIFIC_DATE timetable change requests");
        
        LocalDate today = LocalDate.now();
        List<TimetableChangeRequest> expiredRequests = repository.findApprovedSpecificDateRequestsBeforeDate(today);
        
        if (expiredRequests.isEmpty()) {
            log.info("No expired SPECIFIC_DATE requests found to revert");
            return;
        }
        
        log.info("Found {} expired SPECIFIC_DATE requests to revert", expiredRequests.size());
        
        int revertedCount = 0;
        int errorCount = 0;
        
        for (TimetableChangeRequest request : expiredRequests) {
            try {
                revertChangeRequest(request);
                request.setStatus(TimetableChangeRequest.RequestStatus.COMPLETED);
                repository.save(request);
                revertedCount++;
                log.info("Successfully reverted change request {} for timetable data {}", 
                        request.getId(), request.getTimetableData().getId());
            } catch (Exception e) {
                errorCount++;
                log.error("Failed to revert change request {}: {}", request.getId(), e.getMessage(), e);
            }
        }
        
        log.info("Scheduled task completed. Reverted: {}, Errors: {}, Total: {}", 
                revertedCount, errorCount, expiredRequests.size());
    }

    /**
     * Reverts a specific date change request back to original values
     */
    private void revertChangeRequest(TimetableChangeRequest request) {
        TimetableData timetableData = request.getTimetableData();
        
        if (request.getRequestType() == TimetableChangeRequest.RequestType.ROOM_CHANGE) {
            if (request.getOriginalRoom() == null) {
                throw new IllegalStateException("Original room not found for change request " + request.getId());
            }
            timetableData.setRoom(request.getOriginalRoom());
            log.debug("Reverted room from {} to {} for timetable data {}", 
                    request.getNewRoom() != null ? request.getNewRoom().getName() : "null",
                    request.getOriginalRoom().getName(),
                    timetableData.getId());
        } else if (request.getRequestType() == TimetableChangeRequest.RequestType.PERIOD_CHANGE) {
            if (request.getOriginalTimetableDay() == null || request.getOriginalTimetablePeriod() == null) {
                throw new IllegalStateException("Original day/period not found for change request " + request.getId());
            }
            timetableData.setTimetableDay(request.getOriginalTimetableDay());
            timetableData.setTimetablePeriod(request.getOriginalTimetablePeriod());
            log.debug("Reverted period from {} {} to {} {} for timetable data {}", 
                    request.getNewTimetableDay() != null ? request.getNewTimetableDay().getName() : "null",
                    request.getNewTimetablePeriod() != null ? request.getNewTimetablePeriod().getName() : "null",
                    request.getOriginalTimetableDay().getName(),
                    request.getOriginalTimetablePeriod().getName(),
                    timetableData.getId());
        }
        
        timetableDataRepository.save(timetableData);
    }

}
