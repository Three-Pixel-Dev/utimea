package org.uit.utimea.features.teacher.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.uit.utimea.shared.mapper.MasterDataMapper;
import org.uit.utimea.shared.entity.CodeValue;
import org.uit.utimea.shared.entity.Profile;
import org.uit.utimea.features.teacher.dto.request.TeacherRequest;
import org.uit.utimea.features.teacher.dto.response.CodeValueResponse;
import org.uit.utimea.features.teacher.dto.response.TeacherResponse;
import org.uit.utimea.shared.repository.CodeValueRepository;

@Component
@RequiredArgsConstructor
public class TeacherMapper {

    private final MasterDataMapper masterDataMapper;
    private final CodeValueRepository codeValueRepository;

    public Profile toEntity(TeacherRequest request) {
        Profile.ProfileBuilder builder = Profile.builder()
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .degree(request.degree());
        
        if (request.departmentId() != null) {
            CodeValue department = codeValueRepository.findById(request.departmentId())
                    .orElseThrow(() -> new RuntimeException("CodeValue not found with id: " + request.departmentId()));
            builder.department(department);
        }
        
        return builder.build();
    }

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
        
        return TeacherResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .phoneNumber(entity.getPhoneNumber())
                .degree(entity.getDegree())
                .department(departmentResponse)
                .masterData(masterDataMapper.toMasterData(entity))
                .build();
    }

    public void updateEntity(Profile entity, TeacherRequest request) {
        entity.setName(request.name());
        entity.setPhoneNumber(request.phoneNumber());
        entity.setDegree(request.degree());
        
        if (request.departmentId() != null) {
            CodeValue department = codeValueRepository.findById(request.departmentId())
                    .orElseThrow(() -> new RuntimeException("CodeValue not found with id: " + request.departmentId()));
            entity.setDepartment(department);
        } else {
            entity.setDepartment(null);
        }
    }
}
