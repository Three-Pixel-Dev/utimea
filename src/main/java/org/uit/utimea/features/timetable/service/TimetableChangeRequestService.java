package org.uit.utimea.features.timetable.service;

import org.uit.utimea.features.timetable.dto.request.PeriodChangeRequest;
import org.uit.utimea.features.timetable.dto.request.ProcessChangeRequest;
import org.uit.utimea.features.timetable.dto.request.RoomChangeRequest;
import org.uit.utimea.features.timetable.dto.request.TimetableChangeRequestFilter;
import org.uit.utimea.features.timetable.dto.response.TimetableChangeRequestResponse;
import org.uit.utimea.shared.dto.request.PageAndFilterDTO;
import org.uit.utimea.shared.dto.response.PaginationDTO;

import java.util.List;

public interface TimetableChangeRequestService {
    TimetableChangeRequestResponse requestPeriodChange(PeriodChangeRequest request, Long teacherId);
    TimetableChangeRequestResponse requestRoomChange(RoomChangeRequest request, Long teacherId);
    TimetableChangeRequestResponse processRequest(ProcessChangeRequest request, Long adminId);
    TimetableChangeRequestResponse findById(Long id);
    PaginationDTO<TimetableChangeRequestResponse> getAll(PageAndFilterDTO<TimetableChangeRequestFilter> pageAndFilterDTO);
    List<TimetableChangeRequestResponse> getByTeacherId(Long teacherId);
}
