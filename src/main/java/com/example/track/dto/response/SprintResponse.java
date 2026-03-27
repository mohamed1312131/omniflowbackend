package com.example.track.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class SprintResponse {
    
    private UUID id;
    private String name;
    private String goal;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private UUID projectId;
    private Integer totalTasks;
    private Integer completedTasks;
    private Double progress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
