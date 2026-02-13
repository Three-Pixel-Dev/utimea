package org.uit.utimea.shared.initializr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.uit.utimea.shared.entity.Role;
import org.uit.utimea.shared.repository.RoleRepository;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class RoleInitializr implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        initializeRole("Admin");
        initializeRole("Student");
        initializeRole("Teacher");
    }

    private void initializeRole(String name) {
        roleRepository.findByName(name).orElseGet(() -> {
            Role role = Role.builder()
                    .name(name)
                    .build();
            log.info("Initializing role: {}", name);
            return roleRepository.save(role);
        });
    }
}
