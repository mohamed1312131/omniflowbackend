package com.example.track.controller;

import com.example.track.dto.response.*;
import com.example.track.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard statistics and analytics endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics (Admin only)")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        DashboardStatsResponse stats = dashboardService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/blocked")
    @Operation(summary = "Get all blocked tasks (Admin only)")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getBlockedTasks() {
        List<TaskResponse> tasks = dashboardService.getBlockedTasks();
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @GetMapping("/performance")
    @Operation(summary = "Get developer performance metrics (Admin only)")
    public ResponseEntity<ApiResponse<List<DeveloperPerformanceResponse>>> getPerformance() {
        List<DeveloperPerformanceResponse> performance = dashboardService.getPerformance();
        return ResponseEntity.ok(ApiResponse.success(performance));
    }

    @GetMapping("/activity")
    @Operation(summary = "Get recent activity across all projects (Admin only)")
    public ResponseEntity<ApiResponse<List<ActivityLogResponse>>> getRecentActivity() {
        List<ActivityLogResponse> activity = dashboardService.getRecentActivity();
        return ResponseEntity.ok(ApiResponse.success(activity));
    }
}
