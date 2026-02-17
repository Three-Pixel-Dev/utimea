package org.uit.utimea.features.majorsection.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uit.utimea.shared.dto.request.PageAndFilterDTO;
import org.uit.utimea.shared.dto.response.ApiResponse;
import org.uit.utimea.shared.util.ApiResponseUtil;
import org.uit.utimea.features.majorsection.dto.request.MajorSectionFilter;
import org.uit.utimea.features.majorsection.dto.request.MajorSectionRequest;
import org.uit.utimea.features.majorsection.dto.response.MajorSectionResponse;
import org.uit.utimea.features.majorsection.service.MajorSectionService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/major-sections")
public class MajorSectionController {

    private final MajorSectionService majorSectionService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody MajorSectionRequest request, HttpServletRequest httpServletRequest) {
        MajorSectionResponse response = majorSectionService.create(request);
        ApiResponse apiResponse = ApiResponseUtil.created(
                response,
                "Major section created successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/pageable")
    public ResponseEntity<ApiResponse> getAll(@RequestBody(required = false) PageAndFilterDTO<MajorSectionFilter> pageAndFilterDTO, HttpServletRequest httpServletRequest) {
        if (pageAndFilterDTO == null) {
            pageAndFilterDTO = new PageAndFilterDTO<>();
        }
        var pagination = majorSectionService.getAll(pageAndFilterDTO);
        ApiResponse apiResponse = ApiResponseUtil.paginated(
                pagination,
                "Major sections retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> findById(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        MajorSectionResponse response = majorSectionService.findById(id);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Major section retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @RequestBody MajorSectionRequest request, HttpServletRequest httpServletRequest) {
        MajorSectionResponse response = majorSectionService.update(id, request);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Major section updated successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        majorSectionService.delete(id);
        ApiResponse apiResponse = ApiResponseUtil.noContent(
                "Major section deleted successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<ApiResponse> deleteMany(@RequestBody org.uit.utimea.shared.dto.request.BulkDeleteRequest request, HttpServletRequest httpServletRequest) {
        majorSectionService.deleteMany(request.ids());
        ApiResponse apiResponse = ApiResponseUtil.noContent(
                "Major sections deleted successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }
}
