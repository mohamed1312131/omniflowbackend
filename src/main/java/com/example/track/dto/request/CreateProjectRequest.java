package com.example.track.dto.request;

import com.example.track.validation.HexColor;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

public record CreateProjectRequest(
        @NotBlank(message = "Project name is required")
        String name,
        
        String description,
        
        @HexColor
        String color,
        
        List<UUID> memberIds
) {
}
