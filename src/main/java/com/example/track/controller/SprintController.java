package com.example.track.controller;

import com.example.track.dto.request.CompleteSprintRequest;
import com.example.track.dto.request.CreateSprintRequest;
import com.example.track.dto.request.UpdateSprintRequest;
import com.example.track.dto.response.ApiResponse;
import com.example.track.dto.response.SprintResponse;
import com.example.track.dto.response.StoryWithTasksResponse;
import com.example.track.service.UserStoryService;
import com.example.track.security.SecurityUtils;
import com.example.track.service.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Sprints", description = "Sprint management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SprintController {

    private final SprintService sprintService;
    private final UserStoryService userStoryService;
    private final SecurityUtils securityUtils;

    @GetMapping("/projects/{projectId}/sprints")
    @Operation(summary = "Get all sprints for a project")
    public ResponseEntity<ApiResponse<List<SprintResponse>>> getProjectSprints(@PathVariable UUID projectId) {
        List<SprintResponse> sprints = sprintService.getProjectSprints(projectId);
        return ResponseEntity.ok(ApiResponse.success(sprints));
    }

    @PostMapping("/projects/{projectId}/sprints")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new sprint (Admin only)")
    public ResponseEntity<ApiResponse<SprintResponse>> createSprint(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateSprintRequest request) {
        SprintResponse sprint = sprintService.createSprint(projectId, request, securityUtils.getCurrentUser());
        return ResponseEntity.ok(ApiResponse.success(sprint));
    }

    @GetMapping("/sprints/{id}/board")
    @Operation(summary = "Get all stories with tasks for a sprint board (single query)")
    public ResponseEntity<ApiResponse<List<StoryWithTasksResponse>>> getSprintBoard(@PathVariable UUID id) {
        List<StoryWithTasksResponse> board = userStoryService.getSprintBoard(id);
        return ResponseEntity.ok(ApiResponse.success(board));
    }

    @GetMapping("/sprints/{id}")
    @Operation(summary = "Get sprint by ID")
    public ResponseEntity<ApiResponse<SprintResponse>> getSprint(@PathVariable UUID id) {
        SprintResponse sprint = sprintService.getSprint(id);
        return ResponseEntity.ok(ApiResponse.success(sprint));
    }

    @PutMapping("/sprints/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update sprint (Admin only)")
    public ResponseEntity<ApiResponse<SprintResponse>> updateSprint(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSprintRequest request) {
        SprintResponse sprint = sprintService.updateSprint(id, request);
        return ResponseEntity.ok(ApiResponse.success(sprint));
    }

    @PostMapping("/sprints/{id}/start")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Start sprint (Admin only)")
    public ResponseEntity<ApiResponse<SprintResponse>> startSprint(
            @PathVariable UUID id) {
        SprintResponse sprint = sprintService.startSprint(id, securityUtils.getCurrentUser());
        return ResponseEntity.ok(ApiResponse.success(sprint));
    }

    @PostMapping("/sprints/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Complete sprint (Admin only)")
    public ResponseEntity<ApiResponse<SprintResponse>> completeSprint(
            @PathVariable UUID id,
            @Valid @RequestBody CompleteSprintRequest request) {
        SprintResponse sprint = sprintService.completeSprint(id, request, securityUtils.getCurrentUser());
        return ResponseEntity.ok(ApiResponse.success(sprint));
    }

    @DeleteMapping("/sprints/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete sprint (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteSprint(@PathVariable UUID id) {
        sprintService.deleteSprint(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
