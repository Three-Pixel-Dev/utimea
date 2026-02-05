package org.uit.utimea.features.code.service;

import org.uit.utimea.shared.service.BaseService;
import org.uit.utimea.features.code.dto.request.CodeFilter;
import org.uit.utimea.features.code.dto.request.CodeRequest;
import org.uit.utimea.features.code.dto.response.CodeResponse;

public interface CodeService extends BaseService<CodeRequest, CodeResponse, CodeFilter> {
}
