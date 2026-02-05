package org.uit.utimea.features.code.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uit.utimea.shared.dto.request.PageAndFilterDTO;
import org.uit.utimea.shared.dto.response.ApiResponse;
import org.uit.utimea.shared.util.ApiResponseUtil;
import org.uit.utimea.features.code.dto.request.CodeFilter;
import org.uit.utimea.features.code.dto.request.CodeRequest;
import org.uit.utimea.features.code.dto.response.CodeResponse;
import org.uit.utimea.features.code.service.CodeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/codes")
public class CodeController {

    private final CodeService codeService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody CodeRequest request, HttpServletRequest httpServletRequest) {
        CodeResponse response = codeService.create(request);
        ApiResponse apiResponse = ApiResponseUtil.created(
                response,
                "Code created successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/pageable")
    public ResponseEntity<ApiResponse> getAll(@RequestBody(required = false) PageAndFilterDTO<CodeFilter> pageAndFilterDTO, HttpServletRequest httpServletRequest) {
        if (pageAndFilterDTO == null) {
            pageAndFilterDTO = new PageAndFilterDTO<>();
        }
        var pagination = codeService.getAll(pageAndFilterDTO);
        ApiResponse apiResponse = ApiResponseUtil.paginated(
                pagination,
                "Codes retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> findById(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        CodeResponse response = codeService.findById(id);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Code retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @RequestBody CodeRequest request, HttpServletRequest httpServletRequest) {
        CodeResponse response = codeService.update(id, request);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Code updated successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        codeService.delete(id);
        ApiResponse apiResponse = ApiResponseUtil.noContent(
                "Code deleted successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }
}
