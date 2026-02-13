package org.uit.utimea.shared.initializr;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.uit.utimea.shared.entity.*;
import org.uit.utimea.shared.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(100) // Run after CodeValueInitializer
public class TimetableChangeRequestTestDataInitializer implements CommandLineRunner {

    private final EntityManager entityManager;
    private final CodeRepository codeRepository;
    private final CodeValueRepository codeValueRepository;
    private final MajorSectionRepository majorSectionRepository;
    private final SubjectRepository subjectRepository;
    private final RoomRepository roomRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final TimetableInfoRepository timetableInfoRepository;
    private final TimetableDataRepository timetableDataRepository;
    private final TimetableRepository timetableRepository;
    private final TimetableChangeRequestRepository timetableChangeRequestRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing test data for TimetableChangeRequest...");

        // Get required CodeValues
        CodeValue monday = getCodeValue("Monday", "TIMETABLE_DAYS");
        CodeValue tuesday = getCodeValue("Tuesday", "TIMETABLE_DAYS");
        CodeValue wednesday = getCodeValue("Wednesday", "TIMETABLE_DAYS");
        
        CodeValue period1 = getCodeValue("8:30 - 9:30", "TIMETABLE_PERIODS");
        CodeValue period2 = getCodeValue("9:40 - 10:40", "TIMETABLE_PERIODS");
        CodeValue period3 = getCodeValue("10:50 - 11:50", "TIMETABLE_PERIODS");
        
        CodeValue academicYear = getCodeValue("2025-2026 Academic Year, Semester – (I)", "ACADEMIC_YEAR");
        CodeValue majorSectionYear = getCodeValue("First Year", "MAJOR_SECTION_YEAR");
        CodeValue department = getCodeValue("Department-01", "DEPARTMENT");
        CodeValue roomType = getCodeValue("Lecture", "ROOM_TYPE");
        CodeValue subjectType = getCodeValue("L", "SUBJECT_TYPE");

        // Create or get MajorSection
        MajorSection majorSection = majorSectionRepository.findByName("Test Major Section")
                .orElseGet(() -> {
                    MajorSection ms = MajorSection.builder()
                            .name("Test Major Section")
                            .majorSectionYear(majorSectionYear)
                            .build();
                    log.info("Creating test MajorSection: {}", ms.getName());
                    MajorSection saved = majorSectionRepository.save(ms);
                    entityManager.flush();
                    return saved;
                });

        // Create or get Teacher Profile
        Profile teacher = profileRepository.findAll().stream()
                .filter(p -> p.getDepartment() != null && p.getDepartment().equals(department))
                .findFirst()
                .orElseGet(() -> {
                    Profile p = Profile.builder()
                            .name("Test Teacher")
                            .phoneNumber("1234567890")
                            .degree("PhD")
                            .department(department)
                            .build();
                    log.info("Creating test Teacher Profile: {}", p.getName());
                    Profile saved = profileRepository.save(p);
                    entityManager.flush();
                    return saved;
                });

        // Create or get Admin User
        User admin = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals("admin@test.com"))
                .findFirst()
                .orElseGet(() -> {
                    Role adminRole = roleRepository.findByName("Admin")
                            .orElseThrow(() -> new EntityNotFoundException("Admin role not found. Make sure RoleInitializr runs first."));
                    
                    User u = User.builder()
                            .email("admin@test.com")
                            .password("$2a$10$dummy") // Dummy password hash
                            .role(adminRole)
                            .build();
                    log.info("Creating test Admin User: {}", u.getEmail());
                    User saved = userRepository.save(u);
                    entityManager.flush();
                    return saved;
                });

        // Create or get Subject
        Subject subject = subjectRepository.findAll().stream()
                .filter(s -> s.getCode().equals("TEST-101"))
                .findFirst()
                .orElseGet(() -> {
                    Subject s = Subject.builder()
                            .code("TEST-101")
                            .description("Test Subject")
                            .subjectTypes(List.of(subjectType))
                            .roomType(roomType)
                            .teachers(new ArrayList<>())
                            .build();
                    s.getTeachers().add(teacher);
                    log.info("Creating test Subject: {}", s.getCode());
                    Subject saved = subjectRepository.save(s);
                    entityManager.flush();
                    return saved;
                });

        // Create or get Rooms
        Room room1 = roomRepository.findAll().stream()
                .filter(r -> r.getName().equals("Test Room 1"))
                .findFirst()
                .orElseGet(() -> {
                    Room r = Room.builder()
                            .name("Test Room 1")
                            .capacity(50)
                            .roomType(roomType)
                            .isSpecialRoom(false)
                            .build();
                    log.info("Creating test Room: {}", r.getName());
                    Room saved = roomRepository.save(r);
                    entityManager.flush();
                    return saved;
                });

        Room room2 = roomRepository.findAll().stream()
                .filter(r -> r.getName().equals("Test Room 2"))
                .findFirst()
                .orElseGet(() -> {
                    Room r = Room.builder()
                            .name("Test Room 2")
                            .capacity(60)
                            .roomType(roomType)
                            .isSpecialRoom(false)
                            .build();
                    log.info("Creating test Room: {}", r.getName());
                    Room saved = roomRepository.save(r);
                    entityManager.flush();
                    return saved;
                });

        // Create or get TimetableInfo
        TimetableInfo timetableInfo = timetableInfoRepository.findAll().stream()
                .filter(ti -> ti.getName().equals("Test Timetable Info"))
                .findFirst()
                .orElseGet(() -> {
                    TimetableInfo ti = TimetableInfo.builder()
                            .name("Test Timetable Info")
                            .majorSection(majorSection)
                            .academicYear(academicYear)
                            .build();
                    log.info("Creating test TimetableInfo: {}", ti.getName());
                    TimetableInfo saved = timetableInfoRepository.save(ti);
                    entityManager.flush();
                    return saved;
                });

        // Create or get TimetableData
        TimetableData timetableData = timetableDataRepository.findAll().stream()
                .filter(td -> td.getSubject().getCode().equals("TEST-101") 
                        && td.getTeacher().getId().equals(teacher.getId())
                        && td.getTimetableDay().getName().equals("Monday")
                        && td.getTimetablePeriod().getName().equals("8:30 - 9:30"))
                .findFirst()
                .orElseGet(() -> {
                    TimetableData td = TimetableData.builder()
                            .timetableDay(monday)
                            .timetablePeriod(period1)
                            .subject(subject)
                            .room(room1)
                            .teacher(teacher)
                            .subType("L")
                            .build();
                    log.info("Creating test TimetableData: {} - {} - {}", 
                            td.getSubject().getCode(), 
                            td.getTimetableDay().getName(), 
                            td.getTimetablePeriod().getName());
                    TimetableData saved = timetableDataRepository.save(td);
                    entityManager.flush();
                    return saved;
                });

        // Create or get Timetable
        Timetable timetable = timetableRepository.findAll().stream()
                .filter(t -> t.getTimetableInfo().getId().equals(timetableInfo.getId())
                        && t.getTimetableData().getId().equals(timetableData.getId()))
                .findFirst()
                .orElseGet(() -> {
                    Timetable t = Timetable.builder()
                            .timetableInfo(timetableInfo)
                            .timetableData(timetableData)
                            .build();
                    log.info("Creating test Timetable linking TimetableInfo {} and TimetableData {}", 
                            timetableInfo.getId(), timetableData.getId());
                    Timetable saved = timetableRepository.save(t);
                    entityManager.flush();
                    return saved;
                });

        // Create TimetableChangeRequest entries
        createTimetableChangeRequestIfNotExists(
                timetableData,
                TimetableChangeRequest.RequestType.ROOM_CHANGE,
                TimetableChangeRequest.RequestScope.PERMANENT,
                TimetableChangeRequest.RequestStatus.PENDING,
                room2,
                null,
                null,
                null,
                teacher,
                "Need a larger room for more students",
                null
        );

        createTimetableChangeRequestIfNotExists(
                timetableData,
                TimetableChangeRequest.RequestType.PERIOD_CHANGE,
                TimetableChangeRequest.RequestScope.PERMANENT,
                TimetableChangeRequest.RequestStatus.PENDING,
                null,
                tuesday,
                period2,
                null,
                teacher,
                "Need to change time slot due to personal schedule",
                null
        );

        createTimetableChangeRequestIfNotExists(
                timetableData,
                TimetableChangeRequest.RequestType.ROOM_CHANGE,
                TimetableChangeRequest.RequestScope.SPECIFIC_DATE,
                TimetableChangeRequest.RequestStatus.PENDING,
                room2,
                null,
                null,
                LocalDate.now().plusDays(7), // 7 days from now
                teacher,
                "Temporary room change for next week",
                null
        );

        log.info("Test data initialization for TimetableChangeRequest completed successfully");
    }

    private CodeValue getCodeValue(String name, String codeConstantValue) {
        Code code = codeRepository.findByConstantValue(codeConstantValue)
                .orElseThrow(() -> new EntityNotFoundException("Code with constantValue " + codeConstantValue + " not found."));
        
        return codeValueRepository.findByCodeAndName(code, name)
                .orElseThrow(() -> new EntityNotFoundException("CodeValue with name " + name + " and code " + codeConstantValue + " not found."));
    }

    private void createTimetableChangeRequestIfNotExists(
            TimetableData timetableData,
            TimetableChangeRequest.RequestType requestType,
            TimetableChangeRequest.RequestScope requestScope,
            TimetableChangeRequest.RequestStatus status,
            Room newRoom,
            CodeValue newTimetableDay,
            CodeValue newTimetablePeriod,
            LocalDate specificDate,
            Profile requestedBy,
            String requestReason,
            String adminComment) {
        
        // Check if a similar request already exists
        boolean exists = timetableChangeRequestRepository.findAll().stream()
                .anyMatch(req -> req.getTimetableData().getId().equals(timetableData.getId())
                        && req.getRequestType() == requestType
                        && req.getRequestScope() == requestScope
                        && req.getStatus() == status
                        && (specificDate == null || (req.getSpecificDate() != null && req.getSpecificDate().equals(specificDate))));

        if (!exists) {
            TimetableChangeRequest changeRequest = TimetableChangeRequest.builder()
                    .timetableData(timetableData)
                    .requestType(requestType)
                    .requestScope(requestScope)
                    .status(status)
                    .newRoom(newRoom)
                    .newTimetableDay(newTimetableDay)
                    .newTimetablePeriod(newTimetablePeriod)
                    .specificDate(specificDate)
                    .requestedBy(requestedBy)
                    .requestReason(requestReason)
                    .adminComment(adminComment)
                    .requestedAt(LocalDateTime.now())
                    .build();

            log.info("Creating test TimetableChangeRequest: {} - {} - {}", 
                    requestType, requestScope, status);
            timetableChangeRequestRepository.save(changeRequest);
            entityManager.flush();
        }
    }
}
