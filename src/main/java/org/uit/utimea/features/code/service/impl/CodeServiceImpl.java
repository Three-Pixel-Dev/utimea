package org.uit.utimea.features.code.service.impl;

import org.springframework.stereotype.Service;
import org.uit.utimea.shared.service.impl.BaseServiceImpl;
import org.uit.utimea.shared.entity.Code;
import org.uit.utimea.features.code.dto.request.CodeFilter;
import org.uit.utimea.features.code.dto.request.CodeRequest;
import org.uit.utimea.features.code.dto.response.CodeResponse;
import org.uit.utimea.features.code.mapper.CodeMapper;
import org.uit.utimea.features.code.service.CodeService;
import org.uit.utimea.shared.repository.CodeRepository;

@Service
public class CodeServiceImpl extends BaseServiceImpl<Code, CodeRequest, CodeResponse, CodeFilter> implements CodeService {

    private final CodeMapper codeMapper;

    public CodeServiceImpl(CodeRepository codeRepository, CodeMapper codeMapper) {
        super(codeRepository);
        this.codeMapper = codeMapper;
    }

    @Override
    protected Code mapRequestToEntity(CodeRequest request) {
        return codeMapper.toEntity(request);
    }

    @Override
    protected CodeResponse mapEntityToResponse(Code entity) {
        return codeMapper.toResponse(entity);
    }

    @Override
    protected void updateEntityFromRequest(Code entity, CodeRequest request) {
        codeMapper.updateEntity(entity, request);
    }
}
