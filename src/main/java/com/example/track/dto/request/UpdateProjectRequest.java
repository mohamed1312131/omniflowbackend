package com.example.track.dto.request;

import com.example.track.validation.HexColor;
import jakarta.validation.constraints.NotBlank;

public record UpdateProjectRequest(
        @NotBlank(message = "Project name is required")
        String name,
        
        String description,
        
        @HexColor
        String color,
        
        @NotBlank(message = "Status is required")
        String status
) {
}
