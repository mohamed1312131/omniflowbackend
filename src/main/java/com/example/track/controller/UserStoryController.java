package com.example.track.controller;

import com.example.track.dto.request.BulkCreateStoriesRequest;
import com.example.track.dto.request.CreateStoryRequest;
import com.example.track.dto.request.UpdateStoryRequest;
import com.example.track.dto.response.ApiResponse;
import com.example.track.dto.response.StoryResponse;
import com.example.track.dto.response.StoryWithTasksResponse;
import com.example.track.security.SecurityUtils;
import com.example.track.service.UserStoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "User Stories", description = "User story management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserStoryController {

    private final UserStoryService userStoryService;
    private final SecurityUtils securityUtils;

    @GetMapping("/projects/{projectId}/stories")
    @Operation(summary = "Get all stories for a project")
    public ResponseEntity<ApiResponse<List<StoryResponse>>> getProjectStories(
            @PathVariable UUID projectId,
            @RequestParam(required = false) UUID sprintId,
            @RequestParam(required = false) Boolean backlog) {
        List<StoryResponse> stories = userStoryService.getProjectStories(projectId, sprintId, backlog);
        return ResponseEntity.ok(ApiResponse.success(stories));
    }

    @PostMapping("/projects/{projectId}/stories")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new story (Admin only)")
    public ResponseEntity<ApiResponse<StoryResponse>> createStory(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateStoryRequest request) {
        request.setProjectId(projectId);
        StoryResponse story = userStoryService.createStory(request, securityUtils.getCurrentUser());
        return ResponseEntity.ok(ApiResponse.success(story));
    }

    @PostMapping(value = "/projects/{projectId}/stories/import", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Import stories and tasks from CSV file (Admin only)")
    public ResponseEntity<ApiResponse<List<StoryWithTasksResponse>>> importFromCsv(
            @PathVariable UUID projectId,
            @RequestParam(required = false) UUID sprintId,
            @RequestPart("file") MultipartFile file) {
        List<StoryWithTasksResponse> stories = userStoryService.importFromCsv(projectId, sprintId, file, securityUtils.getCurrentUser());
        return ResponseEntity.ok(ApiResponse.success(stories));
    }

    @PostMapping("/projects/{projectId}/stories/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bulk create stories with tasks (Admin only)")
    public ResponseEntity<ApiResponse<List<StoryWithTasksResponse>>> bulkCreateStories(
            @PathVariable UUID projectId,
            @Valid @RequestBody BulkCreateStoriesRequest request) {
        List<StoryWithTasksResponse> stories = userStoryService.bulkCreateStoriesWithTasks(projectId, request, securityUtils.getCurrentUser());
        return ResponseEntity.ok(ApiResponse.success(stories));
    }

    @GetMapping("/stories/{id}")
    @Operation(summary = "Get story by ID with tasks")
    public ResponseEntity<ApiResponse<StoryWithTasksResponse>> getStory(@PathVariable UUID id) {
        StoryWithTasksResponse story = userStoryService.getStory(id);
        return ResponseEntity.ok(ApiResponse.success(story));
    }

    @PutMapping("/stories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update story (Admin only)")
    public ResponseEntity<ApiResponse<StoryResponse>> updateStory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStoryRequest request) {
        StoryResponse story = userStoryService.updateStory(id, request, securityUtils.getCurrentUser());
        return ResponseEntity.ok(ApiResponse.success(story));
    }

    @DeleteMapping("/stories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete story (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteStory(@PathVariable UUID id) {
        userStoryService.deleteStory(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
