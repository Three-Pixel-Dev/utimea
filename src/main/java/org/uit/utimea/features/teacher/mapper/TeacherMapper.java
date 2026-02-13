package org.uit.utimea.features.teacher.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.uit.utimea.shared.mapper.MasterDataMapper;
import org.uit.utimea.shared.entity.CodeValue;
import org.uit.utimea.shared.entity.Profile;
import org.uit.utimea.shared.entity.Role;
import org.uit.utimea.shared.entity.User;
import org.uit.utimea.features.teacher.dto.request.TeacherRequest;
import org.uit.utimea.features.teacher.dto.response.CodeValueResponse;
import org.uit.utimea.features.teacher.dto.response.TeacherResponse;
import org.uit.utimea.shared.repository.CodeValueRepository;
import org.uit.utimea.shared.repository.RoleRepository;
import org.uit.utimea.shared.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class TeacherMapper {

    private final MasterDataMapper masterDataMapper;
    private final CodeValueRepository codeValueRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Value("${user.default.password.teacher:uit@teacherPsw}")
    private String defaultTeacherPassword;

    @Transactional
    public Profile toEntity(TeacherRequest request) {
        // Create or get User entity
        User user = userRepository.findByEmail(request.email())
                .orElseGet(() -> {
                    Role teacherRole = roleRepository.findByName("Teacher")
                            .orElseThrow(() -> new RuntimeException("Teacher role not found. Make sure RoleInitializr runs first."));
                    
                    User newUser = User.builder()
                            .email(request.email())
                            .password(defaultTeacherPassword)
                            .role(teacherRole)
                            .build();
                    return userRepository.save(newUser);
                });

        Profile.ProfileBuilder builder = Profile.builder()
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .degree(request.degree())
                .user(user);
        
        if (request.departmentId() != null) {
            CodeValue department = codeValueRepository.findById(request.departmentId())
                    .orElseThrow(() -> new RuntimeException("CodeValue not found with id: " + request.departmentId()));
            builder.department(department);
        }
        
        return builder.build();
    }

    @Transactional(readOnly = true)
    public TeacherResponse toResponse(Profile entity) {
        if (entity == null) {
            return null;
        }
        
        CodeValueResponse departmentResponse = null;
        if (entity.getDepartment() != null) {
            departmentResponse = CodeValueResponse.builder()
                    .id(entity.getDepartment().getId())
                    .name(entity.getDepartment().getName())
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
        
        return TeacherResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .phoneNumber(entity.getPhoneNumber())
                .email(email)
                .degree(entity.getDegree())
                .department(departmentResponse)
                .masterData(masterDataMapper.toMasterData(entity))
                .build();
    }

    @Transactional
    public void updateEntity(Profile entity, TeacherRequest request) {
        entity.setName(request.name());
        entity.setPhoneNumber(request.phoneNumber());
        entity.setDegree(request.degree());
        
        // Update or create User entity
        if (request.email() != null && !request.email().isEmpty()) {
            User user = entity.getUser();
            if (user == null) {
                // Create new user
                Role teacherRole = roleRepository.findByName("Teacher")
                        .orElseThrow(() -> new RuntimeException("Teacher role not found. Make sure RoleInitializr runs first."));
                
                user = User.builder()
                        .email(request.email())
                        .password(defaultTeacherPassword)
                        .role(teacherRole)
                        .build();
                user = userRepository.save(user);
                entity.setUser(user);
            } else if (!user.getEmail().equals(request.email())) {
                // Update email if changed
                user.setEmail(request.email());
                userRepository.save(user);
            }
        }
        
        if (request.departmentId() != null) {
            CodeValue department = codeValueRepository.findById(request.departmentId())
                    .orElseThrow(() -> new RuntimeException("CodeValue not found with id: " + request.departmentId()));
            entity.setDepartment(department);
        } else {
            entity.setDepartment(null);
        }
    }
}
