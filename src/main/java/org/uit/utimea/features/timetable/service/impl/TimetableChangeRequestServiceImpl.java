package org.uit.utimea.features.timetable.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uit.utimea.features.timetable.dto.request.PeriodChangeRequest;
import org.uit.utimea.features.timetable.dto.request.ProcessChangeRequest;
import org.uit.utimea.features.timetable.dto.request.RoomChangeRequest;
import org.uit.utimea.features.timetable.dto.request.TimetableChangeRequestFilter;
import org.uit.utimea.features.timetable.dto.response.TimetableChangeRequestResponse;
import org.uit.utimea.features.timetable.mapper.TimetableChangeRequestMapper;
import org.uit.utimea.features.timetable.service.TimetableChangeRequestService;
import org.uit.utimea.features.notification.dto.NotificationRequest;
import org.uit.utimea.features.notification.service.NotificationService;
import org.uit.utimea.shared.dto.request.PageAndFilterDTO;
import org.uit.utimea.shared.dto.response.PaginationDTO;
import org.uit.utimea.shared.entity.Timetable;
import org.uit.utimea.shared.entity.TimetableChangeRequest;
import org.uit.utimea.shared.entity.TimetableData;
import org.uit.utimea.shared.entity.TimetableInfo;
import org.uit.utimea.shared.entity.User;
import org.uit.utimea.shared.repository.*;
import org.uit.utimea.shared.util.PaginationHelper;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimetableChangeRequestServiceImpl implements TimetableChangeRequestService {

    private final TimetableChangeRequestRepository repository;
    private final TimetableChangeRequestMapper mapper;
    private final TimetableRepository timetableRepository;
    private final TimetableDataRepository timetableDataRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public TimetableChangeRequestResponse requestPeriodChange(PeriodChangeRequest request, Long teacherId) {
        log.info("Processing period change request for timetableData {} by teacher {}", request.timetableDataId(), teacherId);

        // Validate request scope
        if ("SPECIFIC_DATE".equals(request.requestScope()) && request.specificDate() == null) {
            throw new IllegalArgumentException("Specific date is required when request scope is SPECIFIC_DATE");
        }

        TimetableData timetableData = timetableDataRepository.findById(request.timetableDataId())
                .orElseThrow(() -> new IllegalArgumentException("TimetableData not found with id: " + request.timetableDataId()));

        // Verify that the teacher owns this timetable data
        if (!timetableData.getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("You can only request changes for your own timetables");
        }

        // Check if request is made at least 3 hours before the scheduled period
        if (request.specificDate() != null) {
            LocalDateTime scheduledDateTime = request.specificDate().atStartOfDay()
                    .plusHours(8); // Assuming periods start at 8 AM, adjust as needed
            LocalDateTime now = LocalDateTime.now();
            long hoursUntilPeriod = ChronoUnit.HOURS.between(now, scheduledDateTime);

            if (hoursUntilPeriod < 3) {
                throw new IllegalArgumentException("Cannot request changes less than 3 hours before the scheduled period");
            }
        }

        // Check if teacher is free at the new time slot, excluding the current timetable data
        boolean teacherBusy = timetableDataRepository.existsByTeacherAndDayAndPeriodExcluding(
                teacherId, request.newTimetableDayId(), request.newTimetablePeriodId(), List.of(timetableData.getId()));
        if (teacherBusy) {
            throw new IllegalArgumentException("Teacher is already assigned to another class at this time slot");
        }

        // Check if the new room is available at the new time slot, excluding the current timetable data
        Long currentRoomId = timetableData.getRoom().getId();
        boolean roomBusy = timetableDataRepository.existsByDayAndPeriodAndRoomExcluding(
                request.newTimetableDayId(), request.newTimetablePeriodId(), currentRoomId, List.of(timetableData.getId()));
        if (roomBusy) {
            throw new IllegalArgumentException("The current room is already occupied at the new time slot");
        }

        // Create the request
        TimetableChangeRequest changeRequest = mapper.toEntityForPeriodChange(request, teacherId);
        TimetableChangeRequest saved = repository.save(changeRequest);

        log.info("Period change request created with id: {}", saved.getId());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TimetableChangeRequestResponse requestRoomChange(RoomChangeRequest request, Long teacherId) {
        log.info("Processing room change request for timetableData {} by teacher {}", request.timetableDataId(), teacherId);

        // Validate request scope
        if ("SPECIFIC_DATE".equals(request.requestScope()) && request.specificDate() == null) {
            throw new IllegalArgumentException("Specific date is required when request scope is SPECIFIC_DATE");
        }

        TimetableData timetableData = timetableDataRepository.findById(request.timetableDataId())
                .orElseThrow(() -> new IllegalArgumentException("TimetableData not found with id: " + request.timetableDataId()));

        // Verify that the teacher owns this timetable data
        if (!timetableData.getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("You can only request changes for your own timetables");
        }

        // Check if request is made at least 3 hours before the scheduled period
        if (request.specificDate() != null) {
            LocalDateTime scheduledDateTime = request.specificDate().atStartOfDay()
                    .plusHours(8); // Assuming periods start at 8 AM, adjust as needed
            LocalDateTime now = LocalDateTime.now();
            long hoursUntilPeriod = ChronoUnit.HOURS.between(now, scheduledDateTime);

            if (hoursUntilPeriod < 3) {
                throw new IllegalArgumentException("Cannot request changes less than 3 hours before the scheduled period");
            }
        }

        // Check if the new room is available at the current time slot, excluding the current timetable data
        Long currentDayId = timetableData.getTimetableDay().getId();
        Long currentPeriodId = timetableData.getTimetablePeriod().getId();
        boolean roomBusy = timetableDataRepository.existsByDayAndPeriodAndRoomExcluding(
                currentDayId, currentPeriodId, request.newRoomId(), List.of(timetableData.getId()));
        if (roomBusy) {
            throw new IllegalArgumentException("The requested room is already occupied at this time slot");
        }

        // Create the request
        TimetableChangeRequest changeRequest = mapper.toEntityForRoomChange(request, teacherId);
        TimetableChangeRequest saved = repository.save(changeRequest);

        log.info("Room change request created with id: {}", saved.getId());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TimetableChangeRequestResponse processRequest(ProcessChangeRequest request, Long adminId) {
        log.info("Processing change request {} with action {} by admin {}", request.requestId(), request.action(), adminId);

        TimetableChangeRequest changeRequest = repository.findById(request.requestId())
                .orElseThrow(() -> new IllegalArgumentException("Change request not found with id: " + request.requestId()));

        if (changeRequest.getStatus() != TimetableChangeRequest.RequestStatus.PENDING) {
            throw new IllegalArgumentException("Only pending requests can be processed");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found with id: " + adminId));

        if ("APPROVE".equalsIgnoreCase(request.action())) {
            changeRequest.setStatus(TimetableChangeRequest.RequestStatus.APPROVED);
            changeRequest.setProcessedBy(admin);
            changeRequest.setProcessedAt(LocalDateTime.now());
            changeRequest.setAdminComment(request.adminComment());

            applyChangeToTimetable(changeRequest);

            log.info("Change request {} approved and applied", request.requestId());
        } else if ("DECLINE".equalsIgnoreCase(request.action())) {
            changeRequest.setStatus(TimetableChangeRequest.RequestStatus.DECLINED);
            changeRequest.setProcessedBy(admin);
            changeRequest.setProcessedAt(LocalDateTime.now());
            changeRequest.setAdminComment(request.adminComment());

            log.info("Change request {} declined", request.requestId());
        } else {
            throw new IllegalArgumentException("Invalid action. Must be APPROVE or DECLINE");
        }

        TimetableChangeRequest saved = repository.save(changeRequest);

        // Send notification after processing the request
        sendNotification(saved);

        return mapper.toResponse(saved);
    }

    private void applyChangeToTimetable(TimetableChangeRequest changeRequest) {
        TimetableData timetableData = changeRequest.getTimetableData();

        // Store original values before applying changes (for SPECIFIC_DATE requests that need to be reverted later)
        if (changeRequest.getRequestScope() == TimetableChangeRequest.RequestScope.SPECIFIC_DATE) {
            if (changeRequest.getOriginalRoom() == null) {
                changeRequest.setOriginalRoom(timetableData.getRoom());
            }
            if (changeRequest.getOriginalTimetableDay() == null) {
                changeRequest.setOriginalTimetableDay(timetableData.getTimetableDay());
            }
            if (changeRequest.getOriginalTimetablePeriod() == null) {
                changeRequest.setOriginalTimetablePeriod(timetableData.getTimetablePeriod());
            }
            // Save the request with original values
            repository.save(changeRequest);
        }

        if (changeRequest.getRequestType() == TimetableChangeRequest.RequestType.ROOM_CHANGE) {
            timetableData.setRoom(changeRequest.getNewRoom());
        } else if (changeRequest.getRequestType() == TimetableChangeRequest.RequestType.PERIOD_CHANGE) {
            timetableData.setTimetableDay(changeRequest.getNewTimetableDay());
            timetableData.setTimetablePeriod(changeRequest.getNewTimetablePeriod());
        }

        timetableDataRepository.save(timetableData);
        log.info("TimetableData {} updated with approved change request", timetableData.getId());
    }

    private void sendNotification(TimetableChangeRequest changeRequest) {
        try {
            TimetableData timetableData = changeRequest.getTimetableData();
            Long teacherId = changeRequest.getRequestedBy().getId();
            
            // Get TimetableInfo from the first related Timetable
            List<Timetable> relatedTimetables = timetableRepository.findAll().stream()
                    .filter(t -> t.getTimetableData().getId().equals(timetableData.getId()))
                    .toList();
            
            if (relatedTimetables.isEmpty()) {
                log.warn("No related timetables found for TimetableData {}, skipping notification", timetableData.getId());
                return;
            }
            
            TimetableInfo timetableInfo = relatedTimetables.get(0).getTimetableInfo();
            Long timetableInfoId = timetableInfo.getId();
            Long majorSectionId = timetableInfo.getMajorSection().getId();

            // Determine action based on request type and status
            String action;
            if (changeRequest.getRequestType() == TimetableChangeRequest.RequestType.ROOM_CHANGE) {
                action = changeRequest.getStatus() == TimetableChangeRequest.RequestStatus.APPROVED
                        ? "ROOM_CHANGE_APPROVED"
                        : "ROOM_CHANGE_DECLINED";
            } else {
                action = changeRequest.getStatus() == TimetableChangeRequest.RequestStatus.APPROVED
                        ? "PERIOD_CHANGE_APPROVED"
                        : "PERIOD_CHANGE_DECLINED";
            }

            NotificationRequest notificationRequest = new NotificationRequest(
                    action,
                    teacherId,
                    timetableInfoId,
                    majorSectionId
            );

            notificationService.createNotification(notificationRequest);
            log.info("Notification sent for change request {} with action {}", changeRequest.getId(), action);
        } catch (Exception e) {
            log.error("Failed to send notification for change request {}", changeRequest.getId(), e);
            // Don't throw exception - notification failure shouldn't break the request processing
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TimetableChangeRequestResponse findById(Long id) {
        TimetableChangeRequest changeRequest = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Change request not found with id: " + id));
        return mapper.toResponse(changeRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationDTO<TimetableChangeRequestResponse> getAll(PageAndFilterDTO<TimetableChangeRequestFilter> pageAndFilterDTO) {
        TimetableChangeRequestFilter filter = pageAndFilterDTO.getFilter();

        Specification<TimetableChangeRequest> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            if (filter != null) {
                if (filter.timetableDataId() != null) {
                    predicates.add(cb.equal(root.get("timetableData").get("id"), filter.timetableDataId()));
                }
                if (filter.requestedById() != null) {
                    predicates.add(cb.equal(root.get("requestedBy").get("id"), filter.requestedById()));
                }
                if (filter.requestType() != null) {
                    predicates.add(cb.equal(root.get("requestType"),
                            TimetableChangeRequest.RequestType.valueOf(filter.requestType())));
                }
                if (filter.status() != null) {
                    predicates.add(cb.equal(root.get("status"),
                            TimetableChangeRequest.RequestStatus.valueOf(filter.status())));
                }
                if (filter.processedById() != null) {
                    predicates.add(cb.equal(root.get("processedBy").get("id"), filter.processedById()));
                }
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        String sortBy = pageAndFilterDTO.getSortBy() != null && !pageAndFilterDTO.getSortBy().isEmpty()
                ? pageAndFilterDTO.getSortBy()
                : "requestedAt";
        String sortDirection = pageAndFilterDTO.getSortDirection() != null && !pageAndFilterDTO.getSortDirection().isEmpty()
                ? pageAndFilterDTO.getSortDirection()
                : "DESC";
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(pageAndFilterDTO.getPage(), pageAndFilterDTO.getSize(), sort);

        Page<TimetableChangeRequest> page = repository.findAll(spec, pageable);
        List<TimetableChangeRequestResponse> content = page.getContent().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return PaginationHelper.getResponse(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimetableChangeRequestResponse> getByTeacherId(Long teacherId) {
        List<TimetableChangeRequest> requests = repository.findByRequestedById(teacherId);
        return requests.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}
