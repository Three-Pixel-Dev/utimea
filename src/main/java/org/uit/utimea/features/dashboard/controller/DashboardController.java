package org.uit.utimea.features.dashboard.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uit.utimea.features.dashboard.dto.response.DashboardResponse;
import org.uit.utimea.features.dashboard.service.DashboardService;
import org.uit.utimea.shared.dto.response.ApiResponse;
import org.uit.utimea.shared.util.ApiResponseUtil;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/counts")
    public ResponseEntity<ApiResponse> getCounts(HttpServletRequest httpServletRequest) {
        DashboardResponse response = dashboardService.getCounts();
        ApiResponse apiResponse = ApiResponseUtil.success(
                response,
                "Dashboard counts retrieved successfully",
                httpServletRequest
        );
        return ResponseEntity.ok(apiResponse);
    }
}
