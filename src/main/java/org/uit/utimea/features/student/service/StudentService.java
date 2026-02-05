package org.uit.utimea.features.student.service;

import org.uit.utimea.shared.service.BaseService;
import org.uit.utimea.features.student.dto.request.StudentFilter;
import org.uit.utimea.features.student.dto.request.StudentRequest;
import org.uit.utimea.features.student.dto.response.StudentResponse;

public interface StudentService extends BaseService<StudentRequest, StudentResponse, StudentFilter> {
}
