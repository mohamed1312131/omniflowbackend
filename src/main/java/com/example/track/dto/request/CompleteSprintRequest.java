package com.example.track.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CompleteSprintRequest {
    
    @NotBlank(message = "Unfinished task action is required")
    private String unfinishedTaskAction; // "MOVE_TO_BACKLOG" or "MOVE_TO_NEXT_SPRINT"
    
    private UUID nextSprintId; // Required if action is MOVE_TO_NEXT_SPRINT
}
