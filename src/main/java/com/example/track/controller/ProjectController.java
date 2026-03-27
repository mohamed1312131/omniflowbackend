package com.example.track.controller;

import com.example.track.dto.request.AddMemberRequest;
import com.example.track.dto.request.CreateProjectRequest;
import com.example.track.dto.request.UpdateProjectRequest;
import com.example.track.dto.response.ApiResponse;
import com.example.track.dto.response.ProjectProgressResponse;
import com.example.track.dto.response.ProjectResponse;
import com.example.track.dto.response.ProjectSummaryResponse;
import com.example.track.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management endpoints")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "Get all projects (Admin: all, Developer: only member projects)")
    public ResponseEntity<ApiResponse<List<ProjectSummaryResponse>>> getAllProjects(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
        
        List<ProjectSummaryResponse> projects = projectService.getAllProjects(currentUserEmail, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new project (Admin only)")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication) {
        String creatorEmail = authentication.getName();
        ProjectResponse project = projectService.createProject(request, creatorEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(project));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID (Developer must be a member)")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(
            @PathVariable UUID id,
            Authentication authentication) {
        
        String currentUserEmail = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
        
        ProjectResponse project = projectService.getProjectById(id, currentUserEmail, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(project));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update project (Admin only)")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request) {
        
        ProjectResponse project = projectService.updateProject(id, request);
        return ResponseEntity.ok(ApiResponse.success(project));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete project (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully"));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add member to project (Admin only)")
    public ResponseEntity<ApiResponse<Void>> addMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddMemberRequest request) {
        
        projectService.addMember(id, request);
        return ResponseEntity.ok(ApiResponse.success("Member added successfully"));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove member from project (Admin only)")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable UUID id,
            @PathVariable UUID userId) {
        
        projectService.removeMember(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully"));
    }

    @GetMapping("/{id}/progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @Operation(summary = "Get project progress with sprints")
    public ResponseEntity<ApiResponse<ProjectProgressResponse>> getProjectProgress(
            @PathVariable UUID id) {
        
        ProjectProgressResponse progress = projectService.getProjectProgress(id);
        return ResponseEntity.ok(ApiResponse.success(progress));
    }
}
