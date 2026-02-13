package org.uit.utimea.features.timetable.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uit.utimea.features.timetable.dto.request.PeriodChangeRequest;
import org.uit.utimea.features.timetable.dto.request.ProcessChangeRequest;
import org.uit.utimea.features.timetable.dto.request.RoomChangeRequest;
import org.uit.utimea.features.timetable.dto.request.TimetableChangeRequestFilter;
import org.uit.utimea.features.timetable.dto.response.TimetableChangeRequestResponse;
import org.uit.utimea.features.timetable.service.TimetableChangeRequestService;
import org.uit.utimea.shared.dto.request.PageAndFilterDTO;
import org.uit.utimea.shared.dto.response.ApiResponse;
import org.uit.utimea.shared.util.ApiResponseUtil;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/timetable-change-requests")
public class TimetableChangeRequestController {

    private final TimetableChangeRequestService changeRequestService;

    @PostMapping("/period-change")
    public ResponseEntity<ApiResponse> requestPeriodChange(
            @RequestBody PeriodChangeRequest request,
            HttpServletRequest httpServletRequest) {
        Long teacherId = getCurrentUserId();
        TimetableChangeRequestResponse response = changeRequestService.requestPeriodChange(request, teacherId);
        ApiResponse apiResponse = ApiResponseUtil.created(
                response,
                "Period change request created successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/room-change")
    public ResponseEntity<ApiResponse> requestRoomChange(
            @RequestBody RoomChangeRequest request,
            HttpServletRequest httpServletRequest) {
        Long teacherId = getCurrentUserId();
        TimetableChangeRequestResponse response = changeRequestService.requestRoomChange(request, teacherId);
        ApiResponse apiResponse = ApiResponseUtil.created(
                response,
                "Room change request created successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/process")
    public ResponseEntity<ApiResponse> processRequest(
            @RequestBody ProcessChangeRequest request,
            HttpServletRequest httpServletRequest) {
        Long adminId = getCurrentUserId();
        TimetableChangeRequestResponse response = changeRequestService.processRequest(request, adminId);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Request processed successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> findById(
            @PathVariable Long id,
            HttpServletRequest httpServletRequest) {
        TimetableChangeRequestResponse response = changeRequestService.findById(id);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Change request retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/pageable")
    public ResponseEntity<ApiResponse> getAll(
            @RequestBody(required = false) PageAndFilterDTO<TimetableChangeRequestFilter> pageAndFilterDTO,
            HttpServletRequest httpServletRequest) {
        if (pageAndFilterDTO == null) {
            pageAndFilterDTO = new PageAndFilterDTO<>();
        }
        var pagination = changeRequestService.getAll(pageAndFilterDTO);
        ApiResponse apiResponse = ApiResponseUtil.paginated(
                pagination,
                "Change requests retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<ApiResponse> getByTeacherId(
            @PathVariable Long teacherId,
            HttpServletRequest httpServletRequest) {
        List<TimetableChangeRequestResponse> response = changeRequestService.getByTeacherId(teacherId);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Teacher change requests retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    private Long getCurrentUserId() {
        return 1L;
    }
}
