package com.example.track.service;

import com.example.track.domain.*;
import com.example.track.dto.request.BulkCreateStoriesRequest;
import com.example.track.dto.request.CreateStoryRequest;
import com.example.track.dto.request.UpdateStoryRequest;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.track.dto.response.StoryResponse;
import com.example.track.dto.response.StoryWithTasksResponse;
import com.example.track.dto.response.TaskResponse;
import com.example.track.dto.response.UserResponse;
import com.example.track.exception.ResourceNotFoundException;
import com.example.track.repository.ProjectRepository;
import com.example.track.repository.SprintRepository;
import com.example.track.repository.TaskRepository;
import com.example.track.repository.UserStoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserStoryService {

    private final UserStoryRepository userStoryRepository;
    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;
    private final ActivityLogService activityLogService;

    public List<StoryResponse> getProjectStories(UUID projectId, UUID sprintId, Boolean backlog) {
        List<UserStory> stories;
        
        if (backlog != null && backlog) {
            stories = userStoryRepository.findByProjectIdAndSprintIdIsNullOrderByPositionAsc(projectId);
        } else if (sprintId != null) {
            stories = userStoryRepository.findByProjectIdAndSprintIdOrderByPositionAsc(projectId, sprintId);
        } else {
            stories = userStoryRepository.findByProjectIdOrderByPositionAsc(projectId);
        }
        
        return stories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public StoryResponse createStory(CreateStoryRequest request, User currentUser) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        Sprint sprint = null;
        if (request.getSprintId() != null) {
            sprint = sprintRepository.findById(request.getSprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
        }

        UserStory.Priority priority = UserStory.Priority.MEDIUM;
        if (request.getPriority() != null) {
            try {
                priority = UserStory.Priority.valueOf(request.getPriority().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Keep default
            }
        }

        UserStory story = UserStory.builder()
                .project(project)
                .sprint(sprint)
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(priority)
                .position(0)
                .createdBy(currentUser)
                .build();

        UserStory saved = userStoryRepository.save(story);

        activityLogService.logActivity("STORY", saved.getId(), currentUser, "created", null);

        return toResponse(saved);
    }

    public List<StoryWithTasksResponse> getSprintBoard(UUID sprintId) {
        return userStoryRepository.findBySprintIdWithTasksOrderByPositionAsc(sprintId)
                .stream()
                .map(this::toDetailedResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<StoryWithTasksResponse> importFromCsv(UUID projectId, UUID sprintId, MultipartFile file, User currentUser) {
        // Parse CSV into grouped structure
        LinkedHashMap<String, BulkCreateStoriesRequest.StoryItem> storiesMap = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            for (CSVRecord record : records) {
                String storyTitle = record.get("User Story");
                String taskTitle  = record.get("Task");
                String priority   = record.get("Priority");
                String description = record.get("Description");

                storiesMap.computeIfAbsent(storyTitle, t -> {
                    BulkCreateStoriesRequest.StoryItem s = new BulkCreateStoriesRequest.StoryItem();
                    s.setTitle(t);
                    s.setTasks(new ArrayList<>());
                    return s;
                });

                BulkCreateStoriesRequest.StoryItem.TaskItem task = new BulkCreateStoriesRequest.StoryItem.TaskItem();
                task.setTitle(taskTitle);
                task.setPriority(priority);
                task.setDescription(description);
                storiesMap.get(storyTitle).getTasks().add(task);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse CSV file: " + e.getMessage());
        }

        BulkCreateStoriesRequest bulkRequest = new BulkCreateStoriesRequest();
        bulkRequest.setSprintId(sprintId);
        bulkRequest.setStories(new ArrayList<>(storiesMap.values()));

        return bulkCreateStoriesWithTasks(projectId, bulkRequest, currentUser);
    }

    @Transactional
    public List<StoryWithTasksResponse> bulkCreateStoriesWithTasks(UUID projectId, BulkCreateStoriesRequest request, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        Sprint sprint = null;
        if (request.getSprintId() != null) {
            sprint = sprintRepository.findById(request.getSprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
        }

        final Sprint resolvedSprint = sprint;

        return request.getStories().stream().map(storyItem -> {
            UserStory.Priority priority = UserStory.Priority.MEDIUM;
            if (storyItem.getPriority() != null) {
                try {
                    priority = UserStory.Priority.valueOf(storyItem.getPriority().toUpperCase());
                } catch (IllegalArgumentException e) { /* keep default */ }
            }

            UserStory story = UserStory.builder()
                    .project(project)
                    .sprint(resolvedSprint)
                    .title(storyItem.getTitle())
                    .description(storyItem.getDescription())
                    .priority(priority)
                    .position(0)
                    .createdBy(currentUser)
                    .build();

            UserStory savedStory = userStoryRepository.save(story);
            activityLogService.logActivity("STORY", savedStory.getId(), currentUser, "created", null);

            if (storyItem.getTasks() != null && !storyItem.getTasks().isEmpty()) {
                List<Task> tasks = storyItem.getTasks().stream().map(taskItem -> {
                    UserStory.Priority taskPriority = UserStory.Priority.MEDIUM;
                    if (taskItem.getPriority() != null) {
                        try {
                            taskPriority = UserStory.Priority.valueOf(taskItem.getPriority().toUpperCase());
                        } catch (IllegalArgumentException e) { /* keep default */ }
                    }

                    Task.TaskStatus status = Task.TaskStatus.TODO;
                    if (taskItem.getStatus() != null) {
                        try {
                            status = Task.TaskStatus.valueOf(taskItem.getStatus().toUpperCase());
                        } catch (IllegalArgumentException e) { /* keep default */ }
                    }

                    return Task.builder()
                            .story(savedStory)
                            .title(taskItem.getTitle())
                            .description(taskItem.getDescription())
                            .status(status)
                            .priority(taskPriority)
                            .position(0)
                            .build();
                }).collect(Collectors.toList());

                List<Task> savedTasks = taskRepository.saveAll(tasks);
                savedTasks.forEach(t -> activityLogService.logActivity("TASK", t.getId(), currentUser, "created", null));
                savedStory.getTasks().addAll(savedTasks);
            }

            return toDetailedResponse(savedStory);
        }).collect(Collectors.toList());
    }

    public StoryWithTasksResponse getStory(UUID id) {
        UserStory story = userStoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Story not found"));
        return toDetailedResponse(story);
    }

    @Transactional
    public StoryResponse updateStory(UUID id, UpdateStoryRequest request, User currentUser) {
        UserStory story = userStoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Story not found"));

        boolean sprintChanged = false;
        String oldSprint = story.getSprint() != null ? story.getSprint().getName() : "Backlog";

        if (request.getTitle() != null) {
            story.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            story.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            try {
                story.setPriority(UserStory.Priority.valueOf(request.getPriority().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Ignore invalid priority
            }
        }
        if (request.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(request.getSprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
            story.setSprint(sprint);
            sprintChanged = true;
        } else if (request.getSprintId() == null && story.getSprint() != null) {
            // Explicitly moving to backlog
            story.setSprint(null);
            sprintChanged = true;
        }

        story.setUpdatedAt(LocalDateTime.now());
        UserStory updated = userStoryRepository.save(story);

        if (sprintChanged) {
            String newSprint = updated.getSprint() != null ? updated.getSprint().getName() : "Backlog";
            activityLogService.logActivity("STORY", updated.getId(), currentUser, "moved_to_sprint",
                    java.util.Map.of("sprint", newSprint, "from", oldSprint));
        }

        return toResponse(updated);
    }

    @Transactional
    public void deleteStory(UUID id) {
        UserStory story = userStoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Story not found"));
        userStoryRepository.delete(story);
    }

    @Transactional
    public int deleteAllStoriesByProjectId(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found");
        }
        List<UserStory> stories = userStoryRepository.findByProjectIdOrderByPositionAsc(projectId);
        userStoryRepository.deleteAll(stories);
        return stories.size();
    }

    private StoryResponse toResponse(UserStory story) {
        int totalTasks = story.getTasks().size();
        int completedTasks = (int) story.getTasks().stream()
                .filter(task -> task.getStatus() == Task.TaskStatus.DONE)
                .count();
        double progress = totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0.0;

        return StoryResponse.builder()
                .id(story.getId())
                .title(story.getTitle())
                .description(story.getDescription())
                .priority(story.getPriority().name())
                .position(story.getPosition())
                .sprintId(story.getSprint() != null ? story.getSprint().getId() : null)
                .sprintName(story.getSprint() != null ? story.getSprint().getName() : null)
                .projectId(story.getProject().getId())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .progress(progress)
                .createdBy(toUserResponse(story.getCreatedBy()))
                .createdAt(story.getCreatedAt())
                .updatedAt(story.getUpdatedAt())
                .build();
    }

    private StoryWithTasksResponse toDetailedResponse(UserStory story) {
        StoryResponse base = toResponse(story);
        
        List<TaskResponse> tasks = story.getTasks().stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList());

        return StoryWithTasksResponse.storyWithTasksBuilder()
                .id(base.getId())
                .title(base.getTitle())
                .description(base.getDescription())
                .priority(base.getPriority())
                .position(base.getPosition())
                .sprintId(base.getSprintId())
                .sprintName(base.getSprintName())
                .projectId(base.getProjectId())
                .totalTasks(base.getTotalTasks())
                .completedTasks(base.getCompletedTasks())
                .progress(base.getProgress())
                .createdBy(base.getCreatedBy())
                .createdAt(base.getCreatedAt())
                .updatedAt(base.getUpdatedAt())
                .tasks(tasks)
                .build();
    }

    private TaskResponse toTaskResponse(Task task) {
        LocalDate today = LocalDate.now();
        boolean isOverdue = task.getDueDate() != null && 
                           task.getDueDate().isBefore(today) && 
                           task.getStatus() != Task.TaskStatus.DONE;
        int daysOverdue = isOverdue ? (int) ChronoUnit.DAYS.between(task.getDueDate(), today) : 0;

        int subTasksTotal = task.getSubTasks().size();
        int subTasksCompleted = (int) task.getSubTasks().stream()
                .filter(SubTask::getIsDone)
                .count();

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .blockedReason(task.getBlockedReason())
                .priority(task.getPriority().name())
                .assignee(task.getAssignee() != null ? toUserResponse(task.getAssignee()) : null)
                .dueDate(task.getDueDate())
                .storyId(task.getStory().getId())
                .storyTitle(task.getStory().getTitle())
                .projectId(task.getStory().getProject().getId())
                .projectName(task.getStory().getProject().getName())
                .isOverdue(isOverdue)
                .daysOverdue(daysOverdue)
                .completedAt(task.getCompletedAt())
                .subTasks(null) // Will be populated by TaskService
                .subTasksCompleted(subTasksCompleted)
                .subTasksTotal(subTasksTotal)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .build();
    }
}
