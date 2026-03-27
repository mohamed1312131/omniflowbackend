package com.example.track.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeveloperPerformanceResponse {
    
    private UserResponse user;
    private Long completedThisWeek;
    private Long completedLastWeek;
    private Long assigned;
    private Long inProgress;
    private Long done;
    private Long blocked;
    private Long overdue;
}
