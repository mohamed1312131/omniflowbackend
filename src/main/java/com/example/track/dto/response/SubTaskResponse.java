package com.example.track.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SubTaskResponse {
    
    private UUID id;
    private String title;
    private Boolean isDone;
    private Integer position;
}
