package org.uit.utimea.features.profile.service.impl;

import org.springframework.stereotype.Service;
import org.uit.utimea.shared.service.impl.BaseServiceImpl;
import org.uit.utimea.shared.entity.Profile;
import org.uit.utimea.features.profile.dto.request.ProfileFilter;
import org.uit.utimea.features.profile.dto.request.ProfileRequest;
import org.uit.utimea.features.profile.dto.response.ProfileResponse;
import org.uit.utimea.features.profile.mapper.ProfileMapper;
import org.uit.utimea.features.profile.service.ProfileService;
import org.uit.utimea.shared.repository.ProfileRepository;

@Service
public class ProfileServiceImpl extends BaseServiceImpl<Profile, ProfileRequest, ProfileResponse, ProfileFilter> implements ProfileService {

    private final ProfileMapper profileMapper;

    public ProfileServiceImpl(ProfileRepository profileRepository, ProfileMapper profileMapper) {
        super(profileRepository);
        this.profileMapper = profileMapper;
    }

    @Override
    protected Profile mapRequestToEntity(ProfileRequest request) {
        return profileMapper.toEntity(request);
    }

    @Override
    protected ProfileResponse mapEntityToResponse(Profile entity) {
        return profileMapper.toResponse(entity);
    }

    @Override
    protected void updateEntityFromRequest(Profile entity, ProfileRequest request) {
        profileMapper.updateEntity(entity, request);
    }
}
