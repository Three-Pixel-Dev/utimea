package org.uit.utimea.shared.initializr;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.uit.utimea.shared.entity.Code;
import org.uit.utimea.shared.entity.CodeValue;
import org.uit.utimea.shared.repository.CodeRepository;
import org.uit.utimea.shared.repository.CodeValueRepository;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(3)
public class CodeValueInitializer implements CommandLineRunner {

    private final CodeRepository codeRepository;
    private final CodeValueRepository codeValueRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        initializeCodeValue("Department-01", "DEPARTMENT");
        initializeCodeValue("Department-02", "DEPARTMENT");
        initializeCodeValue("Department-03", "DEPARTMENT");
        initializeCodeValue("Department-04", "DEPARTMENT");
        initializeCodeValue("Department-05", "DEPARTMENT");

        initializeCodeValue("Batch-01", "BATCH");
        initializeCodeValue("Batch-02", "BATCH");
        initializeCodeValue("Batch-03", "BATCH");
        initializeCodeValue("Batch-04", "BATCH");
        initializeCodeValue("Batch-05", "BATCH");
        initializeCodeValue("Batch-06", "BATCH");
        initializeCodeValue("Batch-07", "BATCH");
        initializeCodeValue("Batch-08", "BATCH");
        initializeCodeValue("Batch-09", "BATCH");
        initializeCodeValue("Batch-10", "BATCH");
        initializeCodeValue("Batch-11", "BATCH");

        initializeCodeValue("2025-2026 Academic Year", "ACADEMIC_YEAR");
        initializeCodeValue("2026-2027 Academic Year", "ACADEMIC_YEAR");
        initializeCodeValue("2027-2028 Academic Year", "ACADEMIC_YEAR");
        initializeCodeValue("2028-2029 Academic Year", "ACADEMIC_YEAR");
        initializeCodeValue("2029-2030 Academic Year", "ACADEMIC_YEAR");
        initializeCodeValue("2030-2031 Academic Year", "ACADEMIC_YEAR");
        initializeCodeValue("2031-2032 Academic Year", "ACADEMIC_YEAR");
        initializeCodeValue("2032-2033 Academic Year", "ACADEMIC_YEAR");
        initializeCodeValue("2033-2034 Academic Year", "ACADEMIC_YEAR");
        initializeCodeValue("2034-2035 Academic Year", "ACADEMIC_YEAR");
        initializeCodeValue("2035-2036 Academic Year", "ACADEMIC_YEAR");
        initializeCodeValue("2036-2037 Academic Year", "ACADEMIC_YEAR");

        initializeCodeValue("First Year", "MAJOR_SECTION_YEAR");
        initializeCodeValue("Second Year", "MAJOR_SECTION_YEAR");
        initializeCodeValue("Third Year", "MAJOR_SECTION_YEAR");
        initializeCodeValue("Fourth Year", "MAJOR_SECTION_YEAR");
        initializeCodeValue("Fifth Year", "MAJOR_SECTION_YEAR");

        initializeCodeValue("Monday", "TIMETABLE_DAYS");
        initializeCodeValue("Tuesday", "TIMETABLE_DAYS");
        initializeCodeValue("Wednesday", "TIMETABLE_DAYS");
        initializeCodeValue("Thursday", "TIMETABLE_DAYS");
        initializeCodeValue("Friday", "TIMETABLE_DAYS");

        initializeCodeValue("8:30 - 9:30", "TIMETABLE_PERIODS");
        initializeCodeValue("9:40 - 10:40", "TIMETABLE_PERIODS");
        initializeCodeValue("10:50 - 11:50", "TIMETABLE_PERIODS");
        initializeCodeValue("11:50 - 12:40", "TIMETABLE_PERIODS");
        initializeCodeValue("12:40 - 1:40", "TIMETABLE_PERIODS");
        initializeCodeValue("1:50 - 2:50", "TIMETABLE_PERIODS");
        initializeCodeValue("3:00 - 4:00", "TIMETABLE_PERIODS");

        initializeCodeValue("L", "SUBJECT_TYPE");
        initializeCodeValue("TDA", "SUBJECT_TYPE");
        initializeCodeValue("Lab", "SUBJECT_TYPE");

        initializeCodeValue("Lecture", "ROOM_TYPE");
        initializeCodeValue("PC", "ROOM_TYPE");
    }

    private void initializeCodeValue(String codeValue, String codeConstantValue) {
        Code code = codeRepository.findByConstantValue(codeConstantValue)
                .orElseThrow(() -> new EntityNotFoundException("Code with constantValue " + codeConstantValue + " not found."));

        if (code.getId() == null) {
            entityManager.persist(code);
            entityManager.flush();
        } else {
            code = entityManager.merge(code);
        }

        Code finalCode = code;
        codeValueRepository.findByCodeAndName(code, codeValue).orElseGet(() -> {
            CodeValue codeValueEntity = CodeValue.builder()
                    .code(finalCode)
                    .name(codeValue)
                    .systemDefined(true)
                    .build();
            log.info("Initializing code value: {} for code: {} (constantValue: {})", codeValue, finalCode.getName(), codeConstantValue);
            CodeValue saved = codeValueRepository.save(codeValueEntity);
            entityManager.flush();
            return saved;
        });
    }
}
