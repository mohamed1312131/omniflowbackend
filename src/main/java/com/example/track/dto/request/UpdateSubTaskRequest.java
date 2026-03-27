package com.example.track.dto.request;

import lombok.Data;

@Data
public class UpdateSubTaskRequest {
    
    private String title;
    private Boolean isDone;
}
