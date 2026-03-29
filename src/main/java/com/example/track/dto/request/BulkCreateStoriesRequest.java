package com.example.track.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BulkCreateStoriesRequest {

    private UUID sprintId; // optional — null means backlog

    @NotEmpty(message = "Stories list must not be empty")
    @Valid
    private List<StoryItem> stories;

    @Data
    public static class StoryItem {

        @NotBlank(message = "Story title is required")
        private String title;

        private String description;
        private String priority; // LOW, MEDIUM, HIGH, CRITICAL

        private List<TaskItem> tasks; // optional

        @Data
        public static class TaskItem {
            @NotBlank(message = "Task title is required")
            private String title;
            private String description;
            private String priority;
            private String status;
        }
    }
}
