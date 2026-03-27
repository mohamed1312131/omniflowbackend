package com.example.track.controller;

import com.example.track.domain.User;
import com.example.track.dto.request.UpdateSubTaskRequest;
import com.example.track.dto.response.ApiResponse;
import com.example.track.dto.response.SubTaskResponse;
import com.example.track.service.SubTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/subtasks")
@RequiredArgsConstructor
@Tag(name = "SubTasks", description = "SubTask management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SubTaskController {

    private final SubTaskService subTaskService;

    @PutMapping("/{id}")
    @Operation(summary = "Update subtask (Admin or assigned developer)")
    public ResponseEntity<ApiResponse<SubTaskResponse>> updateSubTask(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSubTaskRequest request,
            @AuthenticationPrincipal User currentUser) {
        SubTaskResponse subTask = subTaskService.updateSubTask(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success(subTask));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete subtask (Admin or assigned developer)")
    public ResponseEntity<ApiResponse<Void>> deleteSubTask(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        subTaskService.deleteSubTask(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
