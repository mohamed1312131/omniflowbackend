package com.example.track.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CommentResponse {
    
    private UUID id;
    private String content;
    private UserResponse author;
    private LocalDateTime createdAt;
}
