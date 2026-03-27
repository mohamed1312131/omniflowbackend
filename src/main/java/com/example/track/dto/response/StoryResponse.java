package com.example.track.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class StoryResponse {
    
    private UUID id;
    private String title;
    private String description;
    private String priority;
    private Integer position;
    private UUID sprintId;
    private String sprintName;
    private UUID projectId;
    private Integer totalTasks;
    private Integer completedTasks;
    private Double progress;
    private UserResponse createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
