package org.uit.utimea.features.student.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.uit.utimea.shared.mapper.MasterDataMapper;
import org.uit.utimea.shared.entity.CodeValue;
import org.uit.utimea.shared.entity.MajorSection;
import org.uit.utimea.shared.entity.Profile;
import org.uit.utimea.features.student.dto.request.StudentRequest;
import org.uit.utimea.features.student.dto.response.CodeValueResponse;
import org.uit.utimea.features.student.dto.response.MajorSectionResponse;
import org.uit.utimea.features.student.dto.response.StudentResponse;
import org.uit.utimea.shared.repository.CodeValueRepository;
import org.uit.utimea.shared.repository.MajorSectionRepository;

@Component
@RequiredArgsConstructor
public class StudentMapper {

    private final MasterDataMapper masterDataMapper;
    private final CodeValueRepository codeValueRepository;
    private final MajorSectionRepository majorSectionRepository;

    public Profile toEntity(StudentRequest request) {
        Profile.ProfileBuilder builder = Profile.builder()
                .name(request.name())
                .phoneNumber(request.phoneNumber());
        
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
        
        return StudentResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .phoneNumber(entity.getPhoneNumber())
                .batch(batchResponse)
                .majorSection(majorSectionResponse)
                .masterData(masterDataMapper.toMasterData(entity))
                .build();
    }

    public void updateEntity(Profile entity, StudentRequest request) {
        entity.setName(request.name());
        entity.setPhoneNumber(request.phoneNumber());
        
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
