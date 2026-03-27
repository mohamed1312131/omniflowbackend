package com.example.track.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StoryWithTasksResponse extends StoryResponse {
    
    private List<TaskResponse> tasks;
    
    @Builder(builderMethodName = "storyWithTasksBuilder")
    public StoryWithTasksResponse(java.util.UUID id, String title, String description, String priority,
                                   Integer position, java.util.UUID sprintId, String sprintName,
                                   java.util.UUID projectId, Integer totalTasks, Integer completedTasks,
                                   Double progress, UserResponse createdBy,
                                   java.time.LocalDateTime createdAt, java.time.LocalDateTime updatedAt,
                                   List<TaskResponse> tasks) {
        super(id, title, description, priority, position, sprintId, sprintName, projectId,
              totalTasks, completedTasks, progress, createdBy, createdAt, updatedAt);
        this.tasks = tasks;
    }
}
