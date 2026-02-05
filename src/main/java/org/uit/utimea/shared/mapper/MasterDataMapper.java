package org.uit.utimea.shared.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.uit.utimea.shared.dto.response.MasterData;
import org.uit.utimea.shared.entity.MasterEntity;

@Component
@RequiredArgsConstructor
public class MasterDataMapper {

    public MasterData toMasterData(MasterEntity entity) {
        return MasterData.builder()
                .createdBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : 0L)
                .updatedBy(entity.getUpdatedBy() != null ? entity.getUpdatedBy().getId() : 0L)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
