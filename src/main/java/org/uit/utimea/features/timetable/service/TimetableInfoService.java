package org.uit.utimea.features.timetable.service;

import org.uit.utimea.shared.service.BaseService;
import org.uit.utimea.features.timetable.dto.request.TimetableInfoFilter;
import org.uit.utimea.features.timetable.dto.request.TimetableInfoRequest;
import org.uit.utimea.features.timetable.dto.response.TimetableInfoResponse;

public interface TimetableInfoService extends BaseService<TimetableInfoRequest, TimetableInfoResponse, TimetableInfoFilter> {
}
