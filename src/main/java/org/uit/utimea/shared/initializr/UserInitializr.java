package org.uit.utimea.shared.initializr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.uit.utimea.shared.entity.Role;
import org.uit.utimea.shared.entity.User;
import org.uit.utimea.shared.repository.RoleRepository;
import org.uit.utimea.shared.repository.UserRepository;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class UserInitializr implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initializeDefaultAdmin();
        initializeDefaultTeacher();
        initializeDefaultStudent();
    }

    private void initializeDefaultAdmin() {
        Role adminRole = roleRepository.findByName("Admin")
                .orElseThrow(() -> new RuntimeException("Admin role not found"));

        String adminEmail = "admin@utimea.com";
        String adminPassword = "admin@123";

        userRepository.findByEmail(adminEmail).orElseGet(() -> {
            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(adminRole)
                    .build();
            log.info("Initializing default admin user: {}", adminEmail);
            return userRepository.save(admin);
        });
    }

    private void initializeDefaultTeacher() {
        Role teacherRole = roleRepository.findByName("Teacher")
                .orElseThrow(() -> new RuntimeException("Teacher role not found"));

        String teacherEmail = "teacher@utimea.com";
        String teacherPassword = "teacher@123";

        userRepository.findByEmail(teacherEmail).orElseGet(() -> {
            User teacher = User.builder()
                    .email(teacherEmail)
                    .password(passwordEncoder.encode(teacherPassword))
                    .role(teacherRole)
                    .build();
            log.info("Initializing default teacher user: {}", teacherEmail);
            return userRepository.save(teacher);
        });
    }

    private void initializeDefaultStudent() {
        Role studentRole = roleRepository.findByName("Student")
                .orElseThrow(() -> new RuntimeException("Student role not found"));

        String studentEmail = "student@utimea.com";
        String studentPassword = "student@123";

        userRepository.findByEmail(studentEmail).orElseGet(() -> {
            User student = User.builder()
                    .email(studentEmail)
                    .password(passwordEncoder.encode(studentPassword))
                    .role(studentRole)
                    .build();
            log.info("Initializing default student user: {}", studentEmail);
            return userRepository.save(student);
        });
    }
}
