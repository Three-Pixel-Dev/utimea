package org.uit.utimea.features.role.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uit.utimea.shared.dto.request.PageAndFilterDTO;
import org.uit.utimea.shared.dto.response.ApiResponse;
import org.uit.utimea.shared.util.ApiResponseUtil;
import org.uit.utimea.features.role.dto.request.RoleFilter;
import org.uit.utimea.features.role.dto.request.RoleRequest;
import org.uit.utimea.features.role.dto.response.RoleResponse;
import org.uit.utimea.features.role.service.RoleService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody RoleRequest request, HttpServletRequest httpServletRequest) {
        RoleResponse response = roleService.create(request);
        ApiResponse apiResponse = ApiResponseUtil.created(
                response,
                "Role created successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/pageable")
    public ResponseEntity<ApiResponse> getAll(@RequestBody(required = false) PageAndFilterDTO<RoleFilter> pageAndFilterDTO, HttpServletRequest httpServletRequest) {
        if (pageAndFilterDTO == null) {
            pageAndFilterDTO = new PageAndFilterDTO<>();
        }
        var pagination = roleService.getAll(pageAndFilterDTO);
        ApiResponse apiResponse = ApiResponseUtil.paginated(
                pagination,
                "Roles retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> findById(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        RoleResponse response = roleService.findById(id);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Role retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @RequestBody RoleRequest request, HttpServletRequest httpServletRequest) {
        RoleResponse response = roleService.update(id, request);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Role updated successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        roleService.delete(id);
        ApiResponse apiResponse = ApiResponseUtil.noContent(
                "Role deleted successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }
}
