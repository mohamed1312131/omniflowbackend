package com.example.track.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ActivityLogResponse {
    
    private UUID id;
    private String entityType;
    private UUID entityId;
    private String action;
    private String details;
    private UserResponse user;
    private LocalDateTime createdAt;
}
