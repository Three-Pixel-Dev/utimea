package org.uit.utimea.features.majorsection.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.uit.utimea.shared.mapper.MasterDataMapper;
import org.uit.utimea.shared.entity.CodeValue;
import org.uit.utimea.shared.entity.MajorSection;
import org.uit.utimea.features.majorsection.dto.request.MajorSectionRequest;
import org.uit.utimea.features.majorsection.dto.response.CodeValueResponse;
import org.uit.utimea.features.majorsection.dto.response.MajorSectionResponse;
import org.uit.utimea.shared.repository.CodeValueRepository;

@Component
@RequiredArgsConstructor
public class MajorSectionMapper {

    private final MasterDataMapper masterDataMapper;
    private final CodeValueRepository codeValueRepository;

    public MajorSection toEntity(MajorSectionRequest request) {
        MajorSection.MajorSectionBuilder builder = MajorSection.builder()
                .name(request.name());
        
        if (request.majorSectionYearId() != null) {
            CodeValue majorSectionYear = codeValueRepository.findById(request.majorSectionYearId())
                    .orElseThrow(() -> new RuntimeException("CodeValue not found with id: " + request.majorSectionYearId()));
            builder.majorSectionYear(majorSectionYear);
        }
        
        return builder.build();
    }

    public MajorSectionResponse toResponse(MajorSection entity) {
        if (entity == null) {
            return null;
        }
        
        CodeValueResponse majorSectionYearResponse = null;
        if (entity.getMajorSectionYear() != null) {
            majorSectionYearResponse = CodeValueResponse.builder()
                    .id(entity.getMajorSectionYear().getId())
                    .name(entity.getMajorSectionYear().getName())
                    .build();
        }
        
        return MajorSectionResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .majorSectionYear(majorSectionYearResponse)
                .masterData(masterDataMapper.toMasterData(entity))
                .build();
    }

    public void updateEntity(MajorSection entity, MajorSectionRequest request) {
        entity.setName(request.name());
        if (request.majorSectionYearId() != null) {
            CodeValue majorSectionYear = codeValueRepository.findById(request.majorSectionYearId())
                    .orElseThrow(() -> new RuntimeException("CodeValue not found with id: " + request.majorSectionYearId()));
            entity.setMajorSectionYear(majorSectionYear);
        } else {
            entity.setMajorSectionYear(null);
        }
    }
}
