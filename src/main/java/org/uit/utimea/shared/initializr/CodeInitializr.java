package org.uit.utimea.shared.initializr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.uit.utimea.shared.entity.Code;
import org.uit.utimea.shared.repository.CodeRepository;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class CodeInitializr implements CommandLineRunner {

    private final CodeRepository codeRepository;

    @Override
    public void run(String... args) {
        initializeCode("Department", "DEPARTMENT");
        initializeCode("Batch", "BATCH");
        initializeCode("Academic Year", "ACADEMIC_YEAR");
        initializeCode("Major Section Year", "MAJOR_SECTION_YEAR");
    }

    private void initializeCode(String name, String constantValue) {
        codeRepository.findByConstantValue(constantValue).orElseGet(() -> {
            Code code = Code.builder()
                    .name(name)
                    .constantValue(constantValue)
                    .build();
            log.info("Initializing code: {} (constantValue: {})", name, constantValue);
            return codeRepository.save(code);
        });
    }
}
