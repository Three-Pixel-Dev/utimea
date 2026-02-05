package org.uit.utimea.features.codevalue.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.uit.utimea.shared.service.impl.BaseServiceImpl;
import org.uit.utimea.shared.entity.Code;
import org.uit.utimea.shared.entity.CodeValue;
import org.uit.utimea.features.codevalue.dto.request.CodeValueFilter;
import org.uit.utimea.features.codevalue.dto.request.CodeValueRequest;
import org.uit.utimea.features.codevalue.dto.response.CodeValueListResponse;
import org.uit.utimea.features.codevalue.dto.response.CodeValueResponse;
import org.uit.utimea.features.codevalue.mapper.CodeValueMapper;
import org.uit.utimea.features.codevalue.service.CodeValueService;
import org.uit.utimea.shared.repository.CodeRepository;
import org.uit.utimea.shared.repository.CodeValueRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CodeValueServiceImpl extends BaseServiceImpl<CodeValue, CodeValueRequest, CodeValueResponse, CodeValueFilter> implements CodeValueService {

    private final CodeValueMapper codeValueMapper;
    private final CodeRepository codeRepository;
    private final CodeValueRepository codeValueRepository;

    public CodeValueServiceImpl(CodeValueRepository codeValueRepository, CodeValueMapper codeValueMapper,  CodeRepository codeRepository) {
        super(codeValueRepository);
        this.codeValueMapper = codeValueMapper;
        this.codeRepository = codeRepository;
        this.codeValueRepository = codeValueRepository;
    }

    @Override
    protected CodeValue mapRequestToEntity(CodeValueRequest request) {
        return codeValueMapper.toEntity(request);
    }

    @Override
    protected CodeValueResponse mapEntityToResponse(CodeValue entity) {
        return codeValueMapper.toResponse(entity);
    }

    @Override
    protected void updateEntityFromRequest(CodeValue entity, CodeValueRequest request) {
        codeValueMapper.updateEntity(entity, request);
    }

    @Override
    protected Map<String, String> getFieldMapping() {
        return Map.of("codeId", "code.id");
    }

    @Override
    public List<CodeValueListResponse> getCodeValuesByConstantValue(String codeConstantValue) {
        Code code = codeRepository.findByConstantValue(codeConstantValue)
                .orElseThrow(() -> new EntityNotFoundException("Code with constantValue " + codeConstantValue + " not found."));

        List<CodeValue> codeValues = codeValueRepository.findByCode(code);

        return codeValues.stream()
                .map(cv -> CodeValueListResponse.builder()
                        .id(cv.getId())
                        .codeId(cv.getCode().getId())
                        .codeValue(cv.getName())
                        .build())
                .collect(Collectors.toList());
    }
}
