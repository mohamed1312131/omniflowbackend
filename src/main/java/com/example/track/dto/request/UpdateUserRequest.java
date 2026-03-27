package com.example.track.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @NotBlank(message = "Full name is required")
        String fullName,
        
        @NotBlank(message = "Role is required")
        String role,
        
        Boolean isActive
) {
}
