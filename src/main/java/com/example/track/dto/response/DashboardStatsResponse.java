package com.example.track.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsResponse {
    
    private Long activeProjects;
    private Long openTasks;
    private Long overdueTasks;
    private Long completedThisWeek;
}
