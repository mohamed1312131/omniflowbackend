package com.example.track.service;

import com.example.track.domain.*;
import com.example.track.dto.request.BulkCreateTaskRequest;
import com.example.track.dto.request.CreateTaskRequest;
import com.example.track.dto.request.UpdateTaskRequest;
import com.example.track.dto.response.SubTaskResponse;
import com.example.track.dto.response.TaskResponse;
import com.example.track.dto.response.UserResponse;
import com.example.track.exception.ResourceNotFoundException;
import com.example.track.repository.TaskRepository;
import com.example.track.repository.UserRepository;
import com.example.track.repository.UserStoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserStoryRepository userStoryRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public List<TaskResponse> getStoryTasks(UUID storyId) {
        return taskRepository.findByStoryIdOrderByPositionAsc(storyId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request, User currentUser) {
        UserStory story = userStoryRepository.findById(request.getStoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Story not found"));

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
        }

        UserStory.Priority priority = UserStory.Priority.MEDIUM;
        if (request.getPriority() != null) {
            try {
                priority = UserStory.Priority.valueOf(request.getPriority().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Keep default
            }
        }

        Task.TaskStatus status = Task.TaskStatus.TODO;
        if (request.getStatus() != null) {
            try {
                status = Task.TaskStatus.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Keep default
            }
        }

        Task task = Task.builder()
                .story(story)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(status)
                .priority(priority)
                .assignee(assignee)
                .dueDate(request.getDueDate())
                .position(0)
                .build();

        Task saved = taskRepository.save(task);

        activityLogService.logActivity("TASK", saved.getId(), currentUser, "created", null);

        return toResponse(saved);
    }

    @Transactional
    public List<TaskResponse> bulkCreateTasks(UUID storyId, BulkCreateTaskRequest request, User currentUser) {
        UserStory story = userStoryRepository.findById(storyId)
                .orElseThrow(() -> new ResourceNotFoundException("Story not found"));

        List<Task> tasks = request.getTasks().stream().map(item -> {
            UserStory.Priority priority = UserStory.Priority.MEDIUM;
            if (item.getPriority() != null) {
                try {
                    priority = UserStory.Priority.valueOf(item.getPriority().toUpperCase());
                } catch (IllegalArgumentException e) { /* keep default */ }
            }

            Task.TaskStatus status = Task.TaskStatus.TODO;
            if (item.getStatus() != null) {
                try {
                    status = Task.TaskStatus.valueOf(item.getStatus().toUpperCase());
                } catch (IllegalArgumentException e) { /* keep default */ }
            }

            return Task.builder()
                    .story(story)
                    .title(item.getTitle())
                    .description(item.getDescription())
                    .status(status)
                    .priority(priority)
                    .position(0)
                    .build();
        }).collect(Collectors.toList());

        List<Task> saved = taskRepository.saveAll(tasks);
        saved.forEach(t -> activityLogService.logActivity("TASK", t.getId(), currentUser, "created", null));

        return saved.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public TaskResponse getTask(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        return toResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(UUID id, UpdateTaskRequest request, User currentUser) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // CRITICAL: Role-based update logic
        boolean isDeveloper = currentUser.getRole() == User.Role.DEVELOPER;
        
        Task.TaskStatus oldStatus = task.getStatus();
        
        // Developers can ONLY update status and blockedReason
        if (isDeveloper) {
            if (request.getStatus() != null) {
                updateTaskStatus(task, request.getStatus(), request.getBlockedReason(), currentUser);
            }
            if (request.getBlockedReason() != null && task.getStatus() == Task.TaskStatus.BLOCKED) {
                task.setBlockedReason(request.getBlockedReason());
            }
        } else {
            // Admin can update all fields
            if (request.getTitle() != null) {
                task.setTitle(request.getTitle());
            }
            if (request.getDescription() != null) {
                task.setDescription(request.getDescription());
            }
            if (request.getStatus() != null) {
                updateTaskStatus(task, request.getStatus(), request.getBlockedReason(), currentUser);
            }
            if (request.getPriority() != null) {
                try {
                    task.setPriority(UserStory.Priority.valueOf(request.getPriority().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid priority
                }
            }
            if (request.getAssigneeId() != null) {
                User assignee = userRepository.findById(request.getAssigneeId())
                        .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
                task.setAssignee(assignee);
                
                activityLogService.logActivity("TASK", task.getId(), currentUser, "assigned",
                        Map.of("assignee", assignee.getFullName()));
            }
            if (request.getDueDate() != null) {
                task.setDueDate(request.getDueDate());
            }
        }

        task.setUpdatedAt(LocalDateTime.now());
        Task updated = taskRepository.save(task);

        return toResponse(updated);
    }

    private void updateTaskStatus(Task task, String newStatusStr, String blockedReason, User currentUser) {
        Task.TaskStatus newStatus;
        try {
            newStatus = Task.TaskStatus.valueOf(newStatusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return; // Invalid status, ignore
        }

        Task.TaskStatus oldStatus = task.getStatus();
        
        if (oldStatus == newStatus) {
            return; // No change
        }

        // Validate blocked reason
        if (newStatus == Task.TaskStatus.BLOCKED && (blockedReason == null || blockedReason.trim().isEmpty())) {
            throw new IllegalArgumentException("Blocked reason is required when status is BLOCKED");
        }

        task.setStatus(newStatus);

        // Handle status-specific logic
        if (newStatus == Task.TaskStatus.DONE) {
            task.setCompletedAt(LocalDateTime.now());
            activityLogService.logActivity("TASK", task.getId(), currentUser, "completed", null);
        } else if (oldStatus == Task.TaskStatus.DONE) {
            // Moving away from DONE
            task.setCompletedAt(null);
        }

        if (newStatus == Task.TaskStatus.BLOCKED) {
            task.setBlockedReason(blockedReason);
        } else if (oldStatus == Task.TaskStatus.BLOCKED) {
            // Moving away from BLOCKED
            task.setBlockedReason(null);
        }

        // Log status change
        Map<String, Object> details = new HashMap<>();
        details.put("old", oldStatus.name());
        details.put("new", newStatus.name());
        activityLogService.logActivity("TASK", task.getId(), currentUser, "status_changed", details);
    }

    @Transactional
    public void deleteTask(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        taskRepository.delete(task);
    }

    public List<TaskResponse> getUserTasks(UUID userId, User currentUser) {
        // Developers can only query their own tasks
        if (currentUser.getRole() == User.Role.DEVELOPER && !currentUser.getId().equals(userId)) {
            throw new IllegalArgumentException("Developers can only view their own tasks");
        }

        return taskRepository.findByAssigneeIdOrderByPriorityDescDueDateAsc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private TaskResponse toResponse(Task task) {
        LocalDate today = LocalDate.now();
        boolean isOverdue = task.getDueDate() != null && 
                           task.getDueDate().isBefore(today) && 
                           task.getStatus() != Task.TaskStatus.DONE;
        int daysOverdue = isOverdue ? (int) ChronoUnit.DAYS.between(task.getDueDate(), today) : 0;

        List<SubTaskResponse> subTasks = task.getSubTasks().stream()
                .map(st -> SubTaskResponse.builder()
                        .id(st.getId())
                        .title(st.getTitle())
                        .isDone(st.getIsDone())
                        .position(st.getPosition())
                        .build())
                .collect(Collectors.toList());

        int subTasksTotal = subTasks.size();
        int subTasksCompleted = (int) task.getSubTasks().stream()
                .filter(SubTask::getIsDone)
                .count();

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .blockedReason(task.getBlockedReason())
                .priority(task.getPriority().name())
                .assignee(task.getAssignee() != null ? toUserResponse(task.getAssignee()) : null)
                .dueDate(task.getDueDate())
                .storyId(task.getStory().getId())
                .storyTitle(task.getStory().getTitle())
                .projectId(task.getStory().getProject().getId())
                .projectName(task.getStory().getProject().getName())
                .isOverdue(isOverdue)
                .daysOverdue(daysOverdue)
                .completedAt(task.getCompletedAt())
                .subTasks(subTasks)
                .subTasksCompleted(subTasksCompleted)
                .subTasksTotal(subTasksTotal)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .build();
    }
}
