package org.uit.utimea.features.role.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.uit.utimea.shared.mapper.MasterDataMapper;
import org.uit.utimea.shared.entity.Role;
import org.uit.utimea.features.role.dto.request.RoleRequest;
import org.uit.utimea.features.role.dto.response.RoleResponse;

@Component
@RequiredArgsConstructor
public class RoleMapper {

    private final MasterDataMapper masterDataMapper;

    public Role toEntity(RoleRequest request) {
        return Role.builder()
                .name(request.name())
                .build();
    }

    public RoleResponse toResponse(Role entity) {
        if (entity == null) {
            return null;
        }
        return RoleResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .masterData(masterDataMapper.toMasterData(entity))
                .build();
    }

    public void updateEntity(Role entity, RoleRequest request) {
        entity.setName(request.name());
    }
}
