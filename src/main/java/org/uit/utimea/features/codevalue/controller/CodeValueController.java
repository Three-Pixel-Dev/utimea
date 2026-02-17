package org.uit.utimea.features.codevalue.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uit.utimea.shared.dto.request.PageAndFilterDTO;
import org.uit.utimea.shared.dto.response.ApiResponse;
import org.uit.utimea.shared.util.ApiResponseUtil;
import org.uit.utimea.features.codevalue.dto.request.CodeValueFilter;
import org.uit.utimea.features.codevalue.dto.request.CodeValueRequest;
import org.uit.utimea.features.codevalue.dto.response.CodeValueListResponse;
import org.uit.utimea.features.codevalue.dto.response.CodeValueResponse;
import org.uit.utimea.features.codevalue.service.CodeValueService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/code-values")
public class CodeValueController {

    private final CodeValueService codeValueService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody CodeValueRequest request, HttpServletRequest httpServletRequest) {
        CodeValueResponse response = codeValueService.create(request);
        ApiResponse apiResponse = ApiResponseUtil.created(
                response,
                "Code value created successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/pageable")
    public ResponseEntity<ApiResponse> getAll(@RequestBody(required = false) PageAndFilterDTO<CodeValueFilter> pageAndFilterDTO, HttpServletRequest httpServletRequest) {
        if (pageAndFilterDTO == null) {
            pageAndFilterDTO = new PageAndFilterDTO<>();
        }
        var pagination = codeValueService.getAll(pageAndFilterDTO);
        ApiResponse apiResponse = ApiResponseUtil.paginated(
                pagination,
                "Code values retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> findById(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        CodeValueResponse response = codeValueService.findById(id);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Code value retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @RequestBody CodeValueRequest request, HttpServletRequest httpServletRequest) {
        CodeValueResponse response = codeValueService.update(id, request);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Code value updated successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        codeValueService.delete(id);
        ApiResponse apiResponse = ApiResponseUtil.noContent(
                "Code value deleted successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<ApiResponse> deleteMany(@RequestBody org.uit.utimea.shared.dto.request.BulkDeleteRequest request, HttpServletRequest httpServletRequest) {
        codeValueService.deleteMany(request.ids());
        ApiResponse apiResponse = ApiResponseUtil.noContent(
                "Code values deleted successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }

    @GetMapping("/constant-value/{constantValue}")
    public ResponseEntity<ApiResponse> getByConstantValue(@PathVariable String constantValue, HttpServletRequest httpServletRequest) {
        List<CodeValueListResponse> response = codeValueService.getCodeValuesByConstantValue(constantValue);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Code values retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }
}
