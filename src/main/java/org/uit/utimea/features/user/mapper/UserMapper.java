package org.uit.utimea.features.user.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.uit.utimea.shared.mapper.MasterDataMapper;
import org.uit.utimea.shared.entity.Role;
import org.uit.utimea.shared.entity.User;
import org.uit.utimea.features.user.dto.request.UserRequest;
import org.uit.utimea.features.user.dto.response.RoleResponse;
import org.uit.utimea.features.user.dto.response.UserResponse;
import org.uit.utimea.shared.repository.RoleRepository;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final MasterDataMapper masterDataMapper;
    private final RoleRepository roleRepository;

    public User toEntity(UserRequest request) {
        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + request.roleId()));
        
        return User.builder()
                .email(request.email())
                .password(request.password())
                .role(role)
                .build();
    }

    public UserResponse toResponse(User entity) {
        if (entity == null) {
            return null;
        }
        
        RoleResponse roleResponse = null;
        if (entity.getRole() != null) {
            roleResponse = RoleResponse.builder()
                    .id(entity.getRole().getId())
                    .name(entity.getRole().getName())
                    .build();
        }
        
        return UserResponse.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .role(roleResponse)
                .masterData(masterDataMapper.toMasterData(entity))
                .build();
    }

    public void updateEntity(User entity, UserRequest request) {
        entity.setEmail(request.email());
        if (request.password() != null && !request.password().isEmpty()) {
            entity.setPassword(request.password());
        }
        if (request.roleId() != null) {
            Role role = roleRepository.findById(request.roleId())
                    .orElseThrow(() -> new RuntimeException("Role not found with id: " + request.roleId()));
            entity.setRole(role);
        }
    }
}
