package org.uit.utimea.shared.initializr;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.uit.utimea.shared.entity.Code;
import org.uit.utimea.shared.repository.CodeRepository;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class CodeInitializr implements CommandLineRunner {

    private final CodeRepository codeRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        initializeCode("Department", "DEPARTMENT");
        initializeCode("Batch", "BATCH");
        initializeCode("Academic Year", "ACADEMIC_YEAR");
        initializeCode("Major Section Year", "MAJOR_SECTION_YEAR");
        initializeCode("Timetable Days", "TIMETABLE_DAYS");
        initializeCode("Timetable Periods", "TIMETABLE_PERIODS");
        initializeCode("Subject Type", "SUBJECT_TYPE");
        initializeCode("Room Type", "ROOM_TYPE");
        entityManager.flush();
    }

    private void initializeCode(String name, String constantValue) {
        codeRepository.findByConstantValue(constantValue).orElseGet(() -> {
            Code code = Code.builder()
                    .name(name)
                    .constantValue(constantValue)
                    .build();
            log.info("Initializing code: {} (constantValue: {})", name, constantValue);
            Code saved = codeRepository.save(code);
            entityManager.flush();
            return saved;
        });
    }
}
