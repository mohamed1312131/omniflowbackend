package com.example.track.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateStoryRequest {
    
    @NotNull(message = "Project ID is required")
    private UUID projectId;
    
    private UUID sprintId; // Nullable - null means backlog
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    private String priority; // LOW, MEDIUM, HIGH, CRITICAL
}
