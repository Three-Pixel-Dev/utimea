package org.uit.utimea.features.user.service;

import org.uit.utimea.shared.service.BaseService;
import org.uit.utimea.features.user.dto.request.UserFilter;
import org.uit.utimea.features.user.dto.request.UserRequest;
import org.uit.utimea.features.user.dto.response.UserResponse;

public interface UserService extends BaseService<UserRequest, UserResponse, UserFilter> {
}
