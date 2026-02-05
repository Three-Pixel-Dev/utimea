package org.uit.utimea.features.profile.service;

import org.uit.utimea.shared.service.BaseService;
import org.uit.utimea.features.profile.dto.request.ProfileFilter;
import org.uit.utimea.features.profile.dto.request.ProfileRequest;
import org.uit.utimea.features.profile.dto.response.ProfileResponse;

public interface ProfileService extends BaseService<ProfileRequest, ProfileResponse, ProfileFilter> {
}
