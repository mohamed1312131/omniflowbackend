package com.example.track.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class UpdateStoryRequest {
    
    private String title;
    private String description;
    private String priority;
    private UUID sprintId; // Nullable - to move between sprint and backlog
}
