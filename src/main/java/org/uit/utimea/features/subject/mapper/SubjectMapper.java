package org.uit.utimea.features.subject.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.uit.utimea.shared.mapper.MasterDataMapper;
import org.uit.utimea.shared.entity.Subject;
import org.uit.utimea.features.subject.dto.request.SubjectRequest;
import org.uit.utimea.features.subject.dto.response.SubjectResponse;

@Component
@RequiredArgsConstructor
public class SubjectMapper {

    private final MasterDataMapper masterDataMapper;

    public Subject toEntity(SubjectRequest request) {
        return Subject.builder()
                .code(request.code())
                .description(request.description())
                .build();
    }

    public SubjectResponse toResponse(Subject entity) {
        if (entity == null) {
            return null;
        }
        return SubjectResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .description(entity.getDescription())
                .masterData(masterDataMapper.toMasterData(entity))
                .build();
    }

    public void updateEntity(Subject entity, SubjectRequest request) {
        entity.setCode(request.code());
        entity.setDescription(request.description());
    }
}
