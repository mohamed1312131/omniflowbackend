package com.example.track.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TaskResponse {
    
    private UUID id;
    private String title;
    private String description;
    private String status;
    private String blockedReason;
    private String priority;
    private UserResponse assignee;
    private LocalDate dueDate;
    private UUID storyId;
    private String storyTitle;
    private UUID projectId;
    private String projectName;
    private Boolean isOverdue;
    private Integer daysOverdue;
    private LocalDateTime completedAt;
    private List<SubTaskResponse> subTasks;
    private Integer subTasksCompleted;
    private Integer subTasksTotal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
