package org.uit.utimea.features.timetable.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uit.utimea.features.timetable.dto.request.CombineClassRequest;
import org.uit.utimea.features.timetable.dto.request.TimetableFilter;
import org.uit.utimea.features.timetable.dto.request.TimetableRequest;
import org.uit.utimea.features.timetable.dto.response.TimetableResponse;
import org.uit.utimea.features.timetable.mapper.TimetableMapper;
import org.uit.utimea.features.timetable.service.TimetableService;
import org.uit.utimea.features.notification.dto.NotificationRequest;
import org.uit.utimea.features.notification.service.NotificationService;
import org.uit.utimea.features.notification.service.NotificationTextGenerator;
import org.uit.utimea.shared.entity.*;
import org.uit.utimea.shared.repository.*;
import org.uit.utimea.shared.service.impl.BaseServiceImpl;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TimetableServiceImpl extends BaseServiceImpl<Timetable, TimetableRequest, TimetableResponse, TimetableFilter> implements TimetableService {

    private final TimetableMapper timetableMapper;
    private final TimetableRepository timetableRepository;
    private final TimetableDataRepository timetableDataRepository;
    private final ProfileRepository profileRepository;
    private final RoomRepository roomRepository;
    private final CodeValueRepository codeValueRepository;
    private final SubjectRepository subjectRepository;
    private final NotificationService notificationService;
    private final NotificationTextGenerator notificationTextGenerator;

    public TimetableServiceImpl(TimetableRepository timetableRepository, TimetableMapper timetableMapper,
                                TimetableDataRepository timetableDataRepository,
                                ProfileRepository profileRepository,
                                RoomRepository roomRepository,
                                CodeValueRepository codeValueRepository,
                                SubjectRepository subjectRepository,
                                NotificationService notificationService,
                                NotificationTextGenerator notificationTextGenerator) {
        super(timetableRepository);
        this.timetableMapper = timetableMapper;
        this.timetableRepository = timetableRepository;
        this.timetableDataRepository = timetableDataRepository;
        this.profileRepository = profileRepository;
        this.roomRepository = roomRepository;
        this.codeValueRepository = codeValueRepository;
        this.subjectRepository = subjectRepository;
        this.notificationService = notificationService;
        this.notificationTextGenerator = notificationTextGenerator;
    }

    @Override
    protected Timetable mapRequestToEntity(TimetableRequest request) {
        return timetableMapper.toEntity(request);
    }

    @Override
    protected TimetableResponse mapEntityToResponse(Timetable entity) {
        return timetableMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public TimetableResponse update(Long id, TimetableRequest request) {
        Timetable entity = findByIdOrThrow(id);
        TimetableInfo oldTimetableInfo = entity.getTimetableInfo();
        Long oldMajorSectionId = oldTimetableInfo != null ? oldTimetableInfo.getMajorSection().getId() : null;
        Long oldTimetableInfoId = oldTimetableInfo != null ? oldTimetableInfo.getId() : null;
        Long oldTeacherId = entity.getTimetableData() != null && entity.getTimetableData().getTeacher() != null
                ? entity.getTimetableData().getTeacher().getId() : null;

        timetableMapper.updateEntity(entity, request);
        Timetable updatedEntity = repository.save(entity);

        // Send notification for timetable update
        try {
            TimetableInfo newTimetableInfo = updatedEntity.getTimetableInfo();
            Long newMajorSectionId = newTimetableInfo != null ? newTimetableInfo.getMajorSection().getId() : oldMajorSectionId;
            Long newTimetableInfoId = newTimetableInfo != null ? newTimetableInfo.getId() : oldTimetableInfoId;
            Long newTeacherId = updatedEntity.getTimetableData() != null && updatedEntity.getTimetableData().getTeacher() != null
                    ? updatedEntity.getTimetableData().getTeacher().getId() : oldTeacherId;

            String readableText = notificationTextGenerator.generateReadableText(
                    "TIMETABLE_UPDATE", newTeacherId, newTimetableInfoId, newMajorSectionId);

            NotificationRequest notificationRequest = new NotificationRequest(
                    "TIMETABLE_UPDATE",
                    newTeacherId,
                    newTimetableInfoId,
                    newMajorSectionId,
                    readableText
            );

            notificationService.createNotification(notificationRequest);
            log.info("Notification sent for timetable update {}", updatedEntity.getId());
        } catch (Exception e) {
            log.error("Failed to send notification for timetable update {}", updatedEntity.getId(), e);
            // Don't throw exception - notification failure shouldn't break the update
        }

        return mapEntityToResponse(updatedEntity);
    }

    @Override
    protected void updateEntityFromRequest(Timetable entity, TimetableRequest request) {
        timetableMapper.updateEntity(entity, request);
    }

    private Timetable findByIdOrThrow(Long id) {
        return timetableRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Timetable not found with id: " + id));
    }

    @Override
    protected Map<String, String> getFieldMapping() {
        return Map.of(
                "academicYearId", "timetableInfo.academicYear.id",
                "majorSectionId", "timetableInfo.majorSection.id",
                "timetableDayId", "timetableData.timetableDay.id",
                "timetablePeriodId", "timetableData.timetablePeriod.id",
                "subjectId", "timetableData.subject.id",
                "roomId", "timetableData.room.id",
                "teacherId", "timetableData.teacher.id"
        );
    }

    @Override
    public List<TimetableResponse> getByTeacherId(Long teacherId) {
        List<Timetable> timetables = timetableRepository.findByTeacherIdWithAllRelations(teacherId);
        return timetables.stream()
                .map(timetableMapper::toResponse)
                .toList();
    }

    @Override
    public List<TimetableResponse> getByMajorSectionId(Long majorSectionId) {
        List<Timetable> timetables = timetableRepository.findByMajorSectionIdWithAllRelations(majorSectionId);
        return timetables.stream()
                .map(timetableMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void combineClass(CombineClassRequest request) {
        log.info("Combining classes: period1Id={}, period2Id={}, combineDayId={}, combinePeriodId={}, roomId={}, teacherId={}",
                request.period1Id(), request.period2Id(), request.combineDayId(), request.combinePeriodId(), 
                request.roomId(), request.teacherId());

        // Fetch the two timetable entries
        Timetable timetable1 = timetableRepository.findById(request.period1Id())
                .orElseThrow(() -> new RuntimeException("Timetable not found with id: " + request.period1Id()));
        Timetable timetable2 = timetableRepository.findById(request.period2Id())
                .orElseThrow(() -> new RuntimeException("Timetable not found with id: " + request.period2Id()));

        // Get the timetable data for both entries
        TimetableData timetableData1 = timetable1.getTimetableData();
        TimetableData timetableData2 = timetable2.getTimetableData();

        // Get major sections for both timetables
        Long majorSection1Id = timetable1.getTimetableInfo().getMajorSection().getId();
        Long majorSection2Id = timetable2.getTimetableInfo().getMajorSection().getId();

        // 1. Check if teacher is free in that timetable period (excluding the periods we're replacing)
        List<Long> excludeTimetableDataIds = List.of(timetableData1.getId(), timetableData2.getId());
        boolean teacherBusy = timetableDataRepository.existsByTeacherAndDayAndPeriodExcluding(
                request.teacherId(), request.combineDayId(), request.combinePeriodId(), excludeTimetableDataIds);
        if (teacherBusy) {
            throw new RuntimeException("Teacher is already assigned to another class at this time slot");
        }

        // 2. Check if the time slot is free (room + day + period) (excluding the periods we're replacing)
        boolean timeSlotBusy = timetableDataRepository.existsByDayAndPeriodAndRoomExcluding(
                request.combineDayId(), request.combinePeriodId(), request.roomId(), excludeTimetableDataIds);
        if (timeSlotBusy) {
            throw new RuntimeException("This time slot is already occupied by another class");
        }

        // 3. Check room capacity for total students from section 1 and section 2
        long studentsInSection1 = profileRepository.countByMajorSectionId(majorSection1Id);
        long studentsInSection2 = profileRepository.countByMajorSectionId(majorSection2Id);
        long totalStudents = studentsInSection1 + studentsInSection2;

        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + request.roomId()));
        
        if (room.getCapacity() != null && room.getCapacity() < totalStudents) {
            throw new RuntimeException(String.format(
                    "Room capacity (%d) is insufficient for total students (%d) from both sections",
                    room.getCapacity(), totalStudents));
        }

        // 4. Get the subject from timetable data (should be the same for both)
        Subject subject = timetableData1.getSubject();
        if (!subject.getId().equals(timetableData2.getSubject().getId())) {
            throw new RuntimeException("Cannot combine classes with different subjects");
        }

        // Get the teacher
        Profile teacher = profileRepository.findById(request.teacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + request.teacherId()));

        // Get the day and period
        CodeValue combineDay = codeValueRepository.findById(request.combineDayId())
                .orElseThrow(() -> new RuntimeException("Day not found with id: " + request.combineDayId()));
        CodeValue combinePeriod = codeValueRepository.findById(request.combinePeriodId())
                .orElseThrow(() -> new RuntimeException("Period not found with id: " + request.combinePeriodId()));

        // Check if same subject and teacher already exist on the same day (excluding the periods we're replacing)
        boolean duplicateExists = timetableDataRepository.existsBySubjectAndTeacherAndDayExcluding(
                subject.getId(), request.teacherId(), request.combineDayId(), excludeTimetableDataIds);
        
        if (duplicateExists) {
            throw new IllegalArgumentException(
                    String.format("The same subject (%s) and teacher (%s) cannot be assigned to the same day (%s) more than once",
                            subject.getCode(), teacher.getName(), combineDay.getName()));
        }

        // Create new timetable data for the combined class
        TimetableData combinedTimetableData = TimetableData.builder()
                .timetableDay(combineDay)
                .timetablePeriod(combinePeriod)
                .subject(subject)
                .room(room)
                .teacher(teacher)
                .build();

        combinedTimetableData = timetableDataRepository.save(combinedTimetableData);

        // Get timetable info for both sections
        TimetableInfo timetableInfo1 = timetable1.getTimetableInfo();
        TimetableInfo timetableInfo2 = timetable2.getTimetableInfo();

        // Create new timetable entries for both sections pointing to the combined timetable data
        Timetable combinedTimetable1 = Timetable.builder()
                .timetableInfo(timetableInfo1)
                .timetableData(combinedTimetableData)
                .build();

        Timetable combinedTimetable2 = Timetable.builder()
                .timetableInfo(timetableInfo2)
                .timetableData(combinedTimetableData)
                .build();

        timetableRepository.save(combinedTimetable1);
        timetableRepository.save(combinedTimetable2);

        // Check if the old timetable data entries are still referenced by other timetables
        // (excluding the ones we're about to delete)
        long countUsingData1 = timetableRepository.findAll().stream()
                .filter(t -> t.getTimetableData().getId().equals(timetableData1.getId()))
                .filter(t -> !t.getId().equals(timetable1.getId()) && !t.getId().equals(timetable2.getId()))
                .count();
        
        long countUsingData2 = timetableRepository.findAll().stream()
                .filter(t -> t.getTimetableData().getId().equals(timetableData2.getId()))
                .filter(t -> !t.getId().equals(timetable1.getId()) && !t.getId().equals(timetable2.getId()))
                .count();

        // Delete the old timetable entries
        timetableRepository.delete(timetable1);
        timetableRepository.delete(timetable2);

        // If no other timetables are using the old timetable data, delete them to avoid orphaned data
        if (countUsingData1 == 0) {
            timetableDataRepository.delete(timetableData1);
        }

        if (countUsingData2 == 0) {
            timetableDataRepository.delete(timetableData2);
        }

        // Send notifications for both major sections
        try {
            // Notification for section 1
            String readableText1 = notificationTextGenerator.generateReadableText(
                    "COMBINE_CLASS", request.teacherId(), timetableInfo1.getId(), majorSection1Id);
            NotificationRequest notificationRequest1 = new NotificationRequest(
                    "COMBINE_CLASS",
                    request.teacherId(),
                    timetableInfo1.getId(),
                    majorSection1Id,
                    readableText1
            );
            notificationService.createNotification(notificationRequest1);

            // Notification for section 2
            String readableText2 = notificationTextGenerator.generateReadableText(
                    "COMBINE_CLASS", request.teacherId(), timetableInfo2.getId(), majorSection2Id);
            NotificationRequest notificationRequest2 = new NotificationRequest(
                    "COMBINE_CLASS",
                    request.teacherId(),
                    timetableInfo2.getId(),
                    majorSection2Id,
                    readableText2
            );
            notificationService.createNotification(notificationRequest2);

            log.info("Notifications sent for combined classes for sections {} and {}", majorSection1Id, majorSection2Id);
        } catch (Exception e) {
            log.error("Failed to send notifications for combined classes", e);
            // Don't throw exception - notification failure shouldn't break the combine operation
        }

        log.info("Successfully combined classes for sections {} and {}", majorSection1Id, majorSection2Id);
    }
}
