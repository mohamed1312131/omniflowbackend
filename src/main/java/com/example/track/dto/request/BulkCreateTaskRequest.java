package com.example.track.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkCreateTaskRequest {

    @NotEmpty(message = "Tasks list must not be empty")
    @Valid
    private List<BulkTaskItem> tasks;

    @Data
    public static class BulkTaskItem {
        private String title;
        private String description;
        private String priority;
        private String status;
    }
}
