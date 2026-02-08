package org.uit.utimea.features.subject.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.uit.utimea.shared.mapper.MasterDataMapper;
import org.uit.utimea.shared.entity.Subject;
import org.uit.utimea.shared.entity.CodeValue;
import org.uit.utimea.features.subject.dto.request.SubjectRequest;
import org.uit.utimea.features.subject.dto.response.SubjectResponse;
import org.uit.utimea.features.subject.dto.response.CodeValueResponse;
import org.uit.utimea.shared.repository.CodeValueRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SubjectMapper {

    private final MasterDataMapper masterDataMapper;
    private final CodeValueRepository codeValueRepository;

    public Subject toEntity(SubjectRequest request) {
        Subject.SubjectBuilder builder = Subject.builder()
                .code(request.code())
                .description(request.description());

        if (request.subjectTypeIds() != null && !request.subjectTypeIds().isEmpty()) {
            List<CodeValue> subjectTypes = request.subjectTypeIds().stream()
                    .map(id -> codeValueRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("CodeValue not found with id: " + id)))
                    .collect(Collectors.toList());
            builder.subjectTypes(subjectTypes);
        } else {
            builder.subjectTypes(new ArrayList<>());
        }

        if (request.roomTypeId() != null) {
            CodeValue roomType = codeValueRepository.findById(request.roomTypeId())
                    .orElseThrow(() -> new RuntimeException("CodeValue not found with id: " + request.roomTypeId()));
            builder.roomType(roomType);
        }

        return builder.build();
    }

    public SubjectResponse toResponse(Subject entity) {
        if (entity == null) {
            return null;
        }
        
        List<CodeValueResponse> subjectTypesResponse = null;
        if (entity.getSubjectTypes() != null) {
            subjectTypesResponse = entity.getSubjectTypes().stream()
                    .map(subjectType -> CodeValueResponse.builder()
                            .id(subjectType.getId())
                            .name(subjectType.getName())
                            .build())
                    .collect(Collectors.toList());
        }
        
        CodeValueResponse roomTypeResponse = null;
        if (entity.getRoomType() != null) {
            roomTypeResponse = CodeValueResponse.builder()
                    .id(entity.getRoomType().getId())
                    .name(entity.getRoomType().getName())
                    .build();
        }
        
        return SubjectResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .description(entity.getDescription())
                .subjectTypes(subjectTypesResponse)
                .roomType(roomTypeResponse)
                .masterData(masterDataMapper.toMasterData(entity))
                .build();
    }

    public void updateEntity(Subject entity, SubjectRequest request) {
        entity.setCode(request.code());
        entity.setDescription(request.description());

        if (request.subjectTypeIds() != null) {
            if (request.subjectTypeIds().isEmpty()) {
                entity.setSubjectTypes(new ArrayList<>());
            } else {
                List<CodeValue> subjectTypes = request.subjectTypeIds().stream()
                        .map(id -> codeValueRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("CodeValue not found with id: " + id)))
                        .collect(Collectors.toList());
                entity.setSubjectTypes(subjectTypes);
            }
        }

        if (request.roomTypeId() != null) {
            CodeValue roomType = codeValueRepository.findById(request.roomTypeId())
                    .orElseThrow(() -> new RuntimeException("CodeValue not found with id: " + request.roomTypeId()));
            entity.setRoomType(roomType);
        } else {
            entity.setRoomType(null);
        }
    }
}
