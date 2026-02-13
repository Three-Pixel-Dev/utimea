package org.uit.utimea.features.student.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.uit.utimea.shared.mapper.MasterDataMapper;
import org.uit.utimea.shared.entity.CodeValue;
import org.uit.utimea.shared.entity.MajorSection;
import org.uit.utimea.shared.entity.Profile;
import org.uit.utimea.shared.entity.Role;
import org.uit.utimea.shared.entity.User;
import org.uit.utimea.features.student.dto.request.StudentRequest;
import org.uit.utimea.features.student.dto.response.CodeValueResponse;
import org.uit.utimea.features.student.dto.response.MajorSectionResponse;
import org.uit.utimea.features.student.dto.response.StudentResponse;
import org.uit.utimea.shared.repository.CodeValueRepository;
import org.uit.utimea.shared.repository.MajorSectionRepository;
import org.uit.utimea.shared.repository.RoleRepository;
import org.uit.utimea.shared.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class StudentMapper {

    private final MasterDataMapper masterDataMapper;
    private final CodeValueRepository codeValueRepository;
    private final MajorSectionRepository majorSectionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Value("${user.default.password.student:uti@studentPsw}")
    private String defaultStudentPassword;

    @Transactional
    public Profile toEntity(StudentRequest request) {
        // Create or get User entity
        User user = userRepository.findByEmail(request.email())
                .orElseGet(() -> {
                    Role studentRole = roleRepository.findByName("Student")
                            .orElseThrow(() -> new RuntimeException("Student role not found. Make sure RoleInitializr runs first."));
                    
                    User newUser = User.builder()
                            .email(request.email())
                            .password(defaultStudentPassword)
                            .role(studentRole)
                            .build();
                    return userRepository.save(newUser);
                });

        Profile.ProfileBuilder builder = Profile.builder()
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .user(user);
        
        if (request.batchId() != null) {
            CodeValue batch = codeValueRepository.findById(request.batchId())
                    .orElseThrow(() -> new RuntimeException("CodeValue not found with id: " + request.batchId()));
            builder.batch(batch);
        }
        
        if (request.majorSectionId() != null) {
            MajorSection majorSection = majorSectionRepository.findById(request.majorSectionId())
                    .orElseThrow(() -> new RuntimeException("MajorSection not found with id: " + request.majorSectionId()));
            builder.majorSection(majorSection);
        }
        
        return builder.build();
    }

    @Transactional(readOnly = true)
    public StudentResponse toResponse(Profile entity) {
        if (entity == null) {
            return null;
        }
        
        CodeValueResponse batchResponse = null;
        if (entity.getBatch() != null) {
            batchResponse = CodeValueResponse.builder()
                    .id(entity.getBatch().getId())
                    .name(entity.getBatch().getName())
                    .build();
        }
        
        MajorSectionResponse majorSectionResponse = null;
        if (entity.getMajorSection() != null) {
            majorSectionResponse = MajorSectionResponse.builder()
                    .id(entity.getMajorSection().getId())
                    .name(entity.getMajorSection().getName())
                    .build();
        }
        
        // Fetch User email - handle lazy loading
        String email = null;
        try {
            User user = entity.getUser();
            if (user != null) {
                email = user.getEmail();
            }
        } catch (Exception e) {
            // If User is not loaded, try to fetch it by profile id
            // This handles cases where Profile was created before User relationship was added
            email = null;
        }
        
        return StudentResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .phoneNumber(entity.getPhoneNumber())
                .email(email)
                .batch(batchResponse)
                .majorSection(majorSectionResponse)
                .masterData(masterDataMapper.toMasterData(entity))
                .build();
    }

    @Transactional
    public void updateEntity(Profile entity, StudentRequest request) {
        entity.setName(request.name());
        entity.setPhoneNumber(request.phoneNumber());
        
        // Update or create User entity
        if (request.email() != null && !request.email().isEmpty()) {
            User user = entity.getUser();
            if (user == null) {
                // Create new user
                Role studentRole = roleRepository.findByName("Student")
                        .orElseThrow(() -> new RuntimeException("Student role not found. Make sure RoleInitializr runs first."));
                
                user = User.builder()
                        .email(request.email())
                        .password(defaultStudentPassword)
                        .role(studentRole)
                        .build();
                user = userRepository.save(user);
                entity.setUser(user);
            } else if (!user.getEmail().equals(request.email())) {
                // Update email if changed
                user.setEmail(request.email());
                userRepository.save(user);
            }
        }
        
        if (request.batchId() != null) {
            CodeValue batch = codeValueRepository.findById(request.batchId())
                    .orElseThrow(() -> new RuntimeException("CodeValue not found with id: " + request.batchId()));
            entity.setBatch(batch);
        } else {
            entity.setBatch(null);
        }
        
        if (request.majorSectionId() != null) {
            MajorSection majorSection = majorSectionRepository.findById(request.majorSectionId())
                    .orElseThrow(() -> new RuntimeException("MajorSection not found with id: " + request.majorSectionId()));
            entity.setMajorSection(majorSection);
        } else {
            entity.setMajorSection(null);
        }
    }
}
