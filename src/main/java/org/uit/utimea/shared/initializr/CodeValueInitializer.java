package org.uit.utimea.shared.initializr;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.uit.utimea.shared.entity.Code;
import org.uit.utimea.shared.entity.CodeValue;
import org.uit.utimea.shared.repository.CodeRepository;
import org.uit.utimea.shared.repository.CodeValueRepository;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class CodeValueInitializer implements CommandLineRunner {

    private final CodeRepository codeRepository;
    private final CodeValueRepository codeValueRepository;

    @Override
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

        initializeCodeValue("2025-2026 Academic Year, Semester – (I)", "ACADEMIC_YEAR");
        initializeCodeValue("2025-2026 Academic Year, Semester – (II)", "ACADEMIC_YEAR");
        initializeCodeValue("2025-2026 Academic Year, Semester – (III)", "ACADEMIC_YEAR");
        initializeCodeValue("2025-2026 Academic Year, Semester – (IV)", "ACADEMIC_YEAR");
        initializeCodeValue("2025-2026 Academic Year, Semester – (V)", "ACADEMIC_YEAR");
        initializeCodeValue("2025-2026 Academic Year, Semester – (VI)", "ACADEMIC_YEAR");
        initializeCodeValue("2025-2026 Academic Year, Semester – (VII)", "ACADEMIC_YEAR");
        initializeCodeValue("2025-2026 Academic Year, Semester – (VIII)", "ACADEMIC_YEAR");
        initializeCodeValue("2025-2026 Academic Year, Semester – (IX)", "ACADEMIC_YEAR");
        initializeCodeValue("2025-2026 Academic Year, Semester – (X)", "ACADEMIC_YEAR");
        initializeCodeValue("2025-2026 Academic Year, Semester – (XI)", "ACADEMIC_YEAR");
        initializeCodeValue("2025-2026 Academic Year, Semester – (XII)", "ACADEMIC_YEAR");

        initializeCodeValue("First Year, Section - A", "MAJOR_SECTION_YEAR");
        initializeCodeValue("First Year, Section - B", "MAJOR_SECTION_YEAR");
        initializeCodeValue("First Year, Section - C", "MAJOR_SECTION_YEAR");
        initializeCodeValue("First Year, Section - D", "MAJOR_SECTION_YEAR");
        initializeCodeValue("First Year, Section - E", "MAJOR_SECTION_YEAR");
    }

    private void initializeCodeValue(String codeValue, String codeConstantValue) {
        Code code = codeRepository.findByConstantValue(codeConstantValue)
                .orElseThrow(() -> new EntityNotFoundException("Code with constantValue " + codeConstantValue + " not found."));

        codeValueRepository.findByCodeAndName(code, codeValue).orElseGet(() -> {
            CodeValue codeValueEntity = CodeValue.builder()
                    .code(code)
                    .name(codeValue)
                    .build();
            log.info("Initializing code value: {} for code: {} (constantValue: {})", codeValue, code.getName(), codeConstantValue);
            return codeValueRepository.save(codeValueEntity);
        });
    }
}
