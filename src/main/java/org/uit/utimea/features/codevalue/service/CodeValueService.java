package org.uit.utimea.features.codevalue.service;

import org.uit.utimea.shared.service.BaseService;
import org.uit.utimea.features.codevalue.dto.request.CodeValueFilter;
import org.uit.utimea.features.codevalue.dto.request.CodeValueRequest;
import org.uit.utimea.features.codevalue.dto.response.CodeValueListResponse;
import org.uit.utimea.features.codevalue.dto.response.CodeValueResponse;

import java.util.List;

public interface CodeValueService extends BaseService<CodeValueRequest, CodeValueResponse, CodeValueFilter> {
    List<CodeValueListResponse> getCodeValuesByConstantValue(String codeConstantValue);
}
