package org.uit.utimea.features.codevalue.mapper;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.uit.utimea.shared.mapper.MasterDataMapper;
import org.uit.utimea.shared.entity.Code;
import org.uit.utimea.shared.entity.CodeValue;
import org.uit.utimea.features.codevalue.dto.request.CodeValueRequest;
import org.uit.utimea.features.codevalue.dto.response.CodeValueResponse;
import org.uit.utimea.shared.repository.CodeRepository;

@Component
@RequiredArgsConstructor
public class CodeValueMapper {

    private final CodeRepository codeRepository;
    private final MasterDataMapper masterDataMapper;

    public CodeValue toEntity(CodeValueRequest request) {
        Code code = codeRepository.findById(request.codeId())
                .orElseThrow(() -> new EntityNotFoundException("Code not found with id: " + request.codeId()));

        return CodeValue.builder()
                .code(code)
                .name(request.name())
                .systemDefined(request.systemDefined() != null ? request.systemDefined() : true)
                .build();
    }

    public CodeValueResponse toResponse(CodeValue entity) {
        if (entity == null) {
            return null;
        }
        return CodeValueResponse.builder()
                .id(entity.getId())
                .codeId(entity.getCode() != null ? entity.getCode().getId() : null)
                .codeName(entity.getCode() != null ? entity.getCode().getName() : null)
                .codeValue(entity.getName())
                .systemDefined(entity.getSystemDefined() != null ? entity.getSystemDefined() : true)
                .masterData(masterDataMapper.toMasterData(entity))
                .build();
    }

    public void updateEntity(CodeValue entity, CodeValueRequest request) {
        if (request.codeId() != null && (entity.getCode() == null || !request.codeId().equals(entity.getCode().getId()))) {
            Code code = codeRepository.findById(request.codeId())
                    .orElseThrow(() -> new EntityNotFoundException("Code not found with id: " + request.codeId()));
            entity.setCode(code);
        }
        entity.setName(request.name());
        if (request.systemDefined() != null) {
            entity.setSystemDefined(request.systemDefined());
        }
    }
}
