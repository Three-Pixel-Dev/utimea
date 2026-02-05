package org.uit.utimea.features.majorsection.service.impl;

import org.springframework.stereotype.Service;
import org.uit.utimea.shared.service.impl.BaseServiceImpl;
import org.uit.utimea.shared.entity.MajorSection;
import org.uit.utimea.features.majorsection.dto.request.MajorSectionFilter;
import org.uit.utimea.features.majorsection.dto.request.MajorSectionRequest;
import org.uit.utimea.features.majorsection.dto.response.MajorSectionResponse;
import org.uit.utimea.features.majorsection.mapper.MajorSectionMapper;
import org.uit.utimea.features.majorsection.service.MajorSectionService;
import org.uit.utimea.shared.repository.MajorSectionRepository;

@Service
public class MajorSectionServiceImpl extends BaseServiceImpl<MajorSection, MajorSectionRequest, MajorSectionResponse, MajorSectionFilter> implements MajorSectionService {

    private final MajorSectionMapper majorSectionMapper;

    public MajorSectionServiceImpl(MajorSectionRepository majorSectionRepository, MajorSectionMapper majorSectionMapper) {
        super(majorSectionRepository);
        this.majorSectionMapper = majorSectionMapper;
    }

    @Override
    protected MajorSection mapRequestToEntity(MajorSectionRequest request) {
        return majorSectionMapper.toEntity(request);
    }

    @Override
    protected MajorSectionResponse mapEntityToResponse(MajorSection entity) {
        return majorSectionMapper.toResponse(entity);
    }

    @Override
    protected void updateEntityFromRequest(MajorSection entity, MajorSectionRequest request) {
        majorSectionMapper.updateEntity(entity, request);
    }
}
