package org.uit.utimea.features.role.service.impl;

import org.springframework.stereotype.Service;
import org.uit.utimea.shared.service.impl.BaseServiceImpl;
import org.uit.utimea.shared.entity.Role;
import org.uit.utimea.features.role.dto.request.RoleFilter;
import org.uit.utimea.features.role.dto.request.RoleRequest;
import org.uit.utimea.features.role.dto.response.RoleResponse;
import org.uit.utimea.features.role.mapper.RoleMapper;
import org.uit.utimea.features.role.service.RoleService;
import org.uit.utimea.shared.repository.RoleRepository;

@Service
public class RoleServiceImpl extends BaseServiceImpl<Role, RoleRequest, RoleResponse, RoleFilter> implements RoleService {

    private final RoleMapper roleMapper;

    public RoleServiceImpl(RoleRepository roleRepository, RoleMapper roleMapper) {
        super(roleRepository);
        this.roleMapper = roleMapper;
    }

    @Override
    protected Role mapRequestToEntity(RoleRequest request) {
        return roleMapper.toEntity(request);
    }

    @Override
    protected RoleResponse mapEntityToResponse(Role entity) {
        return roleMapper.toResponse(entity);
    }

    @Override
    protected void updateEntityFromRequest(Role entity, RoleRequest request) {
        roleMapper.updateEntity(entity, request);
    }
}
