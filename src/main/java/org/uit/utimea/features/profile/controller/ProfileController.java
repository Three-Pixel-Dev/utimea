package org.uit.utimea.features.profile.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uit.utimea.shared.dto.request.PageAndFilterDTO;
import org.uit.utimea.shared.dto.response.ApiResponse;
import org.uit.utimea.shared.util.ApiResponseUtil;
import org.uit.utimea.features.profile.dto.request.ProfileFilter;
import org.uit.utimea.features.profile.dto.request.ProfileRequest;
import org.uit.utimea.features.profile.dto.response.ProfileResponse;
import org.uit.utimea.features.profile.service.ProfileService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody ProfileRequest request, HttpServletRequest httpServletRequest) {
        ProfileResponse response = profileService.create(request);
        ApiResponse apiResponse = ApiResponseUtil.created(
                response,
                "Profile created successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/pageable")
    public ResponseEntity<ApiResponse> getAll(@RequestBody(required = false) PageAndFilterDTO<ProfileFilter> pageAndFilterDTO, HttpServletRequest httpServletRequest) {
        if (pageAndFilterDTO == null) {
            pageAndFilterDTO = new PageAndFilterDTO<>();
        }
        var pagination = profileService.getAll(pageAndFilterDTO);
        ApiResponse apiResponse = ApiResponseUtil.paginated(
                pagination,
                "Profiles retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> findById(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        ProfileResponse response = profileService.findById(id);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Profile retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @RequestBody ProfileRequest request, HttpServletRequest httpServletRequest) {
        ProfileResponse response = profileService.update(id, request);
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Profile updated successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        profileService.delete(id);
        ApiResponse apiResponse = ApiResponseUtil.noContent(
                "Profile deleted successfully",
                httpServletRequest
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }
}
