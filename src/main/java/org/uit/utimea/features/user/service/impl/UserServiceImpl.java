package org.uit.utimea.features.user.service.impl;

import org.springframework.stereotype.Service;
import org.uit.utimea.shared.service.impl.BaseServiceImpl;
import org.uit.utimea.shared.entity.User;
import org.uit.utimea.features.user.dto.request.UserFilter;
import org.uit.utimea.features.user.dto.request.UserRequest;
import org.uit.utimea.features.user.dto.response.UserResponse;
import org.uit.utimea.features.user.mapper.UserMapper;
import org.uit.utimea.features.user.service.UserService;
import org.uit.utimea.shared.repository.UserRepository;

@Service
public class UserServiceImpl extends BaseServiceImpl<User, UserRequest, UserResponse, UserFilter> implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        super(userRepository);
        this.userMapper = userMapper;
    }

    @Override
    protected User mapRequestToEntity(UserRequest request) {
        return userMapper.toEntity(request);
    }

    @Override
    protected UserResponse mapEntityToResponse(User entity) {
        return userMapper.toResponse(entity);
    }

    @Override
    protected void updateEntityFromRequest(User entity, UserRequest request) {
        userMapper.updateEntity(entity, request);
    }
}
