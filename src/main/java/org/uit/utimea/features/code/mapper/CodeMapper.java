package org.uit.utimea.features.code.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.uit.utimea.shared.mapper.MasterDataMapper;
import org.uit.utimea.shared.entity.Code;
import org.uit.utimea.features.code.dto.request.CodeRequest;
import org.uit.utimea.features.code.dto.response.CodeResponse;

@Component
@RequiredArgsConstructor
public class CodeMapper {

    private final MasterDataMapper masterDataMapper;

    public Code toEntity(CodeRequest request) {
        return Code.builder()
                .name(request.name())
                .constantValue(request.constantValue())
                .build();
    }

    public CodeResponse toResponse(Code entity) {
        if (entity == null) {
            return null;
        }
        return CodeResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .constantValue(entity.getConstantValue())
                .masterData(masterDataMapper.toMasterData(entity))
                .build();
    }

    public void updateEntity(Code entity, CodeRequest request) {
        entity.setName(request.name());
        entity.setConstantValue(request.constantValue());
    }
}
