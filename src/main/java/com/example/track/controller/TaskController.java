package com.example.track.controller;

import com.example.track.dto.request.BulkCreateTaskRequest;
import com.example.track.dto.request.CreateCommentRequest;
import com.example.track.dto.request.CreateSubTaskRequest;
import com.example.track.dto.request.CreateTaskRequest;
import com.example.track.dto.request.UpdateTaskRequest;
import com.example.track.dto.response.*;
import com.example.track.security.SecurityUtils;
import com.example.track.service.ActivityLogService;
import com.example.track.service.CommentService;
import com.example.track.service.SubTaskService;
import com.example.track.service.TaskService;
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
@Tag(name = "Tasks", description = "Task management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;
    private final SubTaskService subTaskService;
    private final CommentService commentService;
    private final ActivityLogService activityLogService;
    private final SecurityUtils securityUtils;

    @GetMapping("/stories/{storyId}/tasks")
    @Operation(summary = "Get all tasks for a story")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getStoryTasks(@PathVariable UUID storyId) {
        List<TaskResponse> tasks = taskService.getStoryTasks(storyId);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @PostMapping("/stories/{storyId}/tasks")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new task (Admin only)")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @PathVariable UUID storyId,
            @Valid @RequestBody CreateTaskRequest request) {
        request.setStoryId(storyId);
        TaskResponse task = taskService.createTask(request, securityUtils.getCurrentUser());
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    @PostMapping("/stories/{storyId}/tasks/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bulk create tasks for a story (Admin only)")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> bulkCreateTasks(
            @PathVariable UUID storyId,
            @Valid @RequestBody BulkCreateTaskRequest request) {
        List<TaskResponse> tasks = taskService.bulkCreateTasks(storyId, request, securityUtils.getCurrentUser());
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @GetMapping("/tasks/{id}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<ApiResponse<TaskResponse>> getTask(@PathVariable UUID id) {
        TaskResponse task = taskService.getTask(id);
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    @PutMapping("/tasks/{id}")
    @Operation(summary = "Update task (Role-based: Admin can update all fields, Developer can only update status and blockedReason)")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request) {
        TaskResponse task = taskService.updateTask(id, request, securityUtils.getCurrentUser());
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    @DeleteMapping("/tasks/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete task (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/users/{userId}/tasks")
    @Operation(summary = "Get all tasks assigned to a user (Developer can only query own tasks)")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getUserTasks(
            @PathVariable UUID userId) {
        List<TaskResponse> tasks = taskService.getUserTasks(userId, securityUtils.getCurrentUser());
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @PostMapping("/tasks/{taskId}/subtasks")
    @Operation(summary = "Add subtask (Admin or assigned developer)")
    public ResponseEntity<ApiResponse<SubTaskResponse>> createSubTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateSubTaskRequest request) {
        SubTaskResponse subTask = subTaskService.createSubTask(taskId, request, securityUtils.getCurrentUser());
        return ResponseEntity.ok(ApiResponse.success(subTask));
    }

    @GetMapping("/tasks/{taskId}/comments")
    @Operation(summary = "Get all comments for a task")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getTaskComments(@PathVariable UUID taskId) {
        List<CommentResponse> comments = commentService.getTaskComments(taskId);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    @PostMapping("/tasks/{taskId}/comments")
    @Operation(summary = "Add comment (Admin or assigned developer)")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateCommentRequest request) {
        CommentResponse comment = commentService.createComment(taskId, request, securityUtils.getCurrentUser());
        return ResponseEntity.ok(ApiResponse.success(comment));
    }

    @GetMapping("/tasks/{taskId}/activity")
    @Operation(summary = "Get activity log for a task")
    public ResponseEntity<ApiResponse<List<ActivityLogResponse>>> getTaskActivity(@PathVariable UUID taskId) {
        List<ActivityLogResponse> activity = activityLogService.getActivityForEntity("TASK", taskId);
        return ResponseEntity.ok(ApiResponse.success(activity));
    }
}
