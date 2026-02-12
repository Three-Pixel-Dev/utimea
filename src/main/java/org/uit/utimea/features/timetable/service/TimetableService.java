package org.uit.utimea.features.timetable.service;

import org.uit.utimea.shared.service.BaseService;
import org.uit.utimea.features.timetable.dto.request.CombineClassRequest;
import org.uit.utimea.features.timetable.dto.request.TimetableFilter;
import org.uit.utimea.features.timetable.dto.request.TimetableRequest;
import org.uit.utimea.features.timetable.dto.response.TimetableResponse;

import java.util.List;

public interface TimetableService extends BaseService<TimetableRequest, TimetableResponse, TimetableFilter> {
    List<TimetableResponse> getByTeacherId(Long teacherId);
    void combineClass(CombineClassRequest request);
}
