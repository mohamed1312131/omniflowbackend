package com.example.track.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UpdateTaskRequest {
    
    private String title;
    private String description;
    private String status;
    private String blockedReason;
    private String priority;
    private UUID assigneeId;
    private LocalDate dueDate;
}
