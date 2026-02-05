package org.uit.utimea.features.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uit.utimea.shared.dto.request.PageAndFilterDTO;
import org.uit.utimea.shared.dto.response.ApiResponse;
import org.uit.utimea.shared.util.ApiResponseUtil;
import org.uit.utimea.features.user.dto.request.UserFilter;
import org.uit.utimea.features.user.dto.request.UserRequest;
import org.uit.utimea.features.user.dto.response.UserResponse;
import org.uit.utimea.features.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody UserRequest request, HttpServletRequest httpServletRequest) {
        UserResponse response = userService.create(request);
        ApiResponse apiResponse = ApiResponseUtil.created(
                response,
                "User created successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/pageable")
    public ResponseEntity<ApiResponse> getAll(@RequestBody(required = false) PageAndFilterDTO<UserFilter> pageAndFilterDTO, HttpServletRequest httpServletRequest) {
        if (pageAndFilterDTO == null) {
            pageAndFilterDTO = new PageAndFilterDTO<>();
        }
        var pagination = userService.getAll(pageAndFilterDTO);
        ApiResponse apiResponse = ApiResponseUtil.paginated(
                pagination,
                "Users retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> findById(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        UserResponse response = userService.findById(id);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "User retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @RequestBody UserRequest request, HttpServletRequest httpServletRequest) {
        UserResponse response = userService.update(id, request);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "User updated successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        userService.delete(id);
        ApiResponse apiResponse = ApiResponseUtil.noContent(
                "User deleted successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }
}
