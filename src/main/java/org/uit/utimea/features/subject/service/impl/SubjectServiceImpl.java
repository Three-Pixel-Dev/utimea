package org.uit.utimea.features.subject.service.impl;

import org.springframework.stereotype.Service;
import org.uit.utimea.shared.service.impl.BaseServiceImpl;
import org.uit.utimea.shared.entity.Subject;
import org.uit.utimea.features.subject.dto.request.SubjectFilter;
import org.uit.utimea.features.subject.dto.request.SubjectRequest;
import org.uit.utimea.features.subject.dto.response.SubjectResponse;
import org.uit.utimea.features.subject.mapper.SubjectMapper;
import org.uit.utimea.features.subject.service.SubjectService;
import org.uit.utimea.shared.repository.SubjectRepository;

@Service
public class SubjectServiceImpl extends BaseServiceImpl<Subject, SubjectRequest, SubjectResponse, SubjectFilter> implements SubjectService {

    private final SubjectMapper subjectMapper;

    public SubjectServiceImpl(SubjectRepository subjectRepository, SubjectMapper subjectMapper) {
        super(subjectRepository);
        this.subjectMapper = subjectMapper;
    }

    @Override
    protected Subject mapRequestToEntity(SubjectRequest request) {
        return subjectMapper.toEntity(request);
    }

    @Override
    protected SubjectResponse mapEntityToResponse(Subject entity) {
        return subjectMapper.toResponse(entity);
    }

    @Override
    protected void updateEntityFromRequest(Subject entity, SubjectRequest request) {
        subjectMapper.updateEntity(entity, request);
    }
}
