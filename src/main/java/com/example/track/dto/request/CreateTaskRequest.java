package com.example.track.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateTaskRequest {
    
    @NotNull(message = "Story ID is required")
    private UUID storyId;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    private String priority;
    private UUID assigneeId;
    private LocalDate dueDate;
    private String status; // Default TODO
}
