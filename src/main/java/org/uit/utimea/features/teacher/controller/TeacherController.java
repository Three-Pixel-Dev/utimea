package org.uit.utimea.features.teacher.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uit.utimea.shared.dto.request.PageAndFilterDTO;
import org.uit.utimea.shared.dto.response.ApiResponse;
import org.uit.utimea.shared.util.ApiResponseUtil;
import org.uit.utimea.features.teacher.dto.request.TeacherFilter;
import org.uit.utimea.features.teacher.dto.request.TeacherRequest;
import org.uit.utimea.features.teacher.dto.response.TeacherResponse;
import org.uit.utimea.features.teacher.service.TeacherService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody TeacherRequest request, HttpServletRequest httpServletRequest) {
        TeacherResponse response = teacherService.create(request);
        ApiResponse apiResponse = ApiResponseUtil.created(
                response,
                "Teacher created successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/pageable")
    public ResponseEntity<ApiResponse> getAll(@RequestBody(required = false) PageAndFilterDTO<TeacherFilter> pageAndFilterDTO, HttpServletRequest httpServletRequest) {
        if (pageAndFilterDTO == null) {
            pageAndFilterDTO = new PageAndFilterDTO<>();
        }
        var pagination = teacherService.getAll(pageAndFilterDTO);
        ApiResponse apiResponse = ApiResponseUtil.paginated(
                pagination,
                "Teachers retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> findById(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        TeacherResponse response = teacherService.findById(id);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Teacher retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @RequestBody TeacherRequest request, HttpServletRequest httpServletRequest) {
        TeacherResponse response = teacherService.update(id, request);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Teacher updated successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        teacherService.delete(id);
        ApiResponse apiResponse = ApiResponseUtil.noContent(
                "Teacher deleted successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<ApiResponse> deleteMany(@RequestBody org.uit.utimea.shared.dto.request.BulkDeleteRequest request, HttpServletRequest httpServletRequest) {
        teacherService.deleteMany(request.ids());
        ApiResponse apiResponse = ApiResponseUtil.noContent(
                "Teachers deleted successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }
}
