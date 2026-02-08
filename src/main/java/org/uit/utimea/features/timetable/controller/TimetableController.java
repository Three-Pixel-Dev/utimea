package org.uit.utimea.features.timetable.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uit.utimea.features.timetable.dto.request.TimetableGenerationRequest;
import org.uit.utimea.features.timetable.dto.response.TimetableResponseDto;
import org.uit.utimea.features.timetable.service.impl.TimetableGenerationService;
import org.uit.utimea.shared.dto.request.PageAndFilterDTO;
import org.uit.utimea.shared.dto.response.ApiResponse;
import org.uit.utimea.shared.util.ApiResponseUtil;
import org.uit.utimea.features.timetable.dto.request.TimetableFilter;
import org.uit.utimea.features.timetable.dto.request.TimetableRequest;
import org.uit.utimea.features.timetable.dto.request.TimetableInfoFilter;
import org.uit.utimea.features.timetable.dto.request.TimetableInfoRequest;
import org.uit.utimea.features.timetable.dto.response.TimetableResponse;
import org.uit.utimea.features.timetable.dto.response.TimetableInfoResponse;
import org.uit.utimea.features.timetable.dto.response.TimetableInfoWithTimetablesResponse;
import org.uit.utimea.features.timetable.service.TimetableService;
import org.uit.utimea.features.timetable.service.TimetableInfoService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/timetables")
public class TimetableController {

    private final TimetableService timetableService;
    private final TimetableInfoService timetableInfoService;
    private final TimetableGenerationService generationService;
    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody TimetableRequest request, HttpServletRequest httpServletRequest) {
        TimetableResponse response = timetableService.create(request);
        ApiResponse apiResponse = ApiResponseUtil.created(
                response,
                "Timetable created successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/pageable")
    public ResponseEntity<ApiResponse> getAll(@RequestBody(required = false) PageAndFilterDTO<TimetableFilter> pageAndFilterDTO, HttpServletRequest httpServletRequest) {
        if (pageAndFilterDTO == null) {
            pageAndFilterDTO = new PageAndFilterDTO<>();
        }
        var pagination = timetableService.getAll(pageAndFilterDTO);
        ApiResponse apiResponse = ApiResponseUtil.paginated(
                pagination,
                "Timetables retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> findById(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        TimetableResponse response = timetableService.findById(id);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Timetable retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @RequestBody TimetableRequest request, HttpServletRequest httpServletRequest) {
        TimetableResponse response = timetableService.update(id, request);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Timetable updated successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        timetableService.delete(id);
        ApiResponse apiResponse = ApiResponseUtil.noContent(
                "Timetable deleted successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateTimetable(@RequestBody TimetableGenerationRequest request) {
        try {
            generationService.generateTimetable(request);
            return ResponseEntity.ok("Timetable generated successfully for Section ID: " + request.getMajorSectionId());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Generation Failed: " + e.getMessage());
        }
    }

//    @GetMapping("/view/{majorSectionId}")
//    public ResponseEntity<List<TimetableResponseDto>> getTimetable(@PathVariable Long majorSectionId) {
//        List<TimetableResponseDto> timetable = generationService.getTimetableBySection(majorSectionId);
//        return ResponseEntity.ok(timetable);
//    }

    // TimetableInfo endpoints
    @PostMapping("/info")
    public ResponseEntity<ApiResponse> createInfo(@RequestBody TimetableInfoRequest request, HttpServletRequest httpServletRequest) {
        TimetableInfoResponse response = timetableInfoService.create(request);
        ApiResponse apiResponse = ApiResponseUtil.created(
                response,
                "TimetableInfo created successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/info/pageable")
    public ResponseEntity<ApiResponse> getAllInfo(@RequestBody(required = false) PageAndFilterDTO<TimetableInfoFilter> pageAndFilterDTO, HttpServletRequest httpServletRequest) {
        if (pageAndFilterDTO == null) {
            pageAndFilterDTO = new PageAndFilterDTO<>();
        }
        var pagination = timetableInfoService.getAll(pageAndFilterDTO);
        ApiResponse apiResponse = ApiResponseUtil.paginated(
                pagination,
                "TimetableInfos retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/info/{id}")
    public ResponseEntity<ApiResponse> findInfoById(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        TimetableInfoWithTimetablesResponse response = timetableInfoService.findByIdWithTimetables(id);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "TimetableInfo with timetables retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/info/{id}")
    public ResponseEntity<ApiResponse> updateInfo(@PathVariable Long id, @RequestBody TimetableInfoRequest request, HttpServletRequest httpServletRequest) {
        TimetableInfoResponse response = timetableInfoService.update(id, request);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "TimetableInfo updated successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/info/{id}")
    public ResponseEntity<ApiResponse> deleteInfo(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        timetableInfoService.delete(id);
        ApiResponse apiResponse = ApiResponseUtil.noContent(
                "TimetableInfo deleted successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }
}
