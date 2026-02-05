package org.uit.utimea.features.teacher.service;

import org.uit.utimea.shared.service.BaseService;
import org.uit.utimea.features.teacher.dto.request.TeacherFilter;
import org.uit.utimea.features.teacher.dto.request.TeacherRequest;
import org.uit.utimea.features.teacher.dto.response.TeacherResponse;

public interface TeacherService extends BaseService<TeacherRequest, TeacherResponse, TeacherFilter> {
}
