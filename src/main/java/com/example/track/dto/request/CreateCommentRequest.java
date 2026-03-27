package com.example.track.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCommentRequest {
    
    @NotBlank(message = "Content is required")
    private String content;
}
