package org.uit.utimea.features.subject.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.uit.utimea.shared.mapper.MasterDataMapper;
import org.uit.utimea.shared.entity.Subject;
import org.uit.utimea.shared.entity.CodeValue;
import org.uit.utimea.shared.entity.Profile;
import org.uit.utimea.features.subject.dto.request.SubjectRequest;
import org.uit.utimea.features.subject.dto.response.SubjectResponse;
import org.uit.utimea.features.subject.dto.response.CodeValueResponse;
import org.uit.utimea.features.teacher.dto.response.TeacherResponse;
import org.uit.utimea.features.teacher.mapper.TeacherMapper;
import org.uit.utimea.shared.repository.CodeValueRepository;
import org.uit.utimea.shared.repository.ProfileRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SubjectMapper {

    private final MasterDataMapper masterDataMapper;
    private final CodeValueRepository codeValueRepository;
    private final ProfileRepository profileRepository;
    private final TeacherMapper teacherMapper;

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

        if (request.teacherIds() != null && !request.teacherIds().isEmpty()) {
            List<Profile> teachers = request.teacherIds().stream()
                    .map(id -> profileRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id)))
                    .collect(Collectors.toList());
            builder.teachers(teachers);
        } else {
            builder.teachers(new ArrayList<>());
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

        List<TeacherResponse> teachersResponse = null;
        if (entity.getTeachers() != null) {
            teachersResponse = entity.getTeachers().stream()
                    .map(teacherMapper::toResponse)
                    .collect(Collectors.toList());
        }
        
        return SubjectResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .description(entity.getDescription())
                .subjectTypes(subjectTypesResponse)
                .roomType(roomTypeResponse)
                .teachers(teachersResponse)
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

        if (request.teacherIds() != null) {
            if (request.teacherIds().isEmpty()) {
                entity.setTeachers(new ArrayList<>());
            } else {
                List<Profile> teachers = request.teacherIds().stream()
                        .map(id -> profileRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id)))
                        .collect(Collectors.toList());
                entity.setTeachers(teachers);
            }
        }
    }
}
