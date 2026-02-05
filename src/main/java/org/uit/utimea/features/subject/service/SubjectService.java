package org.uit.utimea.features.subject.service;

import org.uit.utimea.shared.service.BaseService;
import org.uit.utimea.features.subject.dto.request.SubjectFilter;
import org.uit.utimea.features.subject.dto.request.SubjectRequest;
import org.uit.utimea.features.subject.dto.response.SubjectResponse;

public interface SubjectService extends BaseService<SubjectRequest, SubjectResponse, SubjectFilter> {
}
