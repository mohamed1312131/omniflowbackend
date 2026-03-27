package com.example.track.service;

import com.example.track.domain.Project;
import com.example.track.domain.Sprint;
import com.example.track.domain.Sprint.SprintStatus;
import com.example.track.domain.Task;
import com.example.track.domain.User;
import com.example.track.domain.UserStory;
import com.example.track.dto.request.CompleteSprintRequest;
import com.example.track.dto.request.CreateSprintRequest;
import com.example.track.dto.request.UpdateSprintRequest;
import com.example.track.dto.response.SprintResponse;
import com.example.track.exception.ResourceNotFoundException;
import com.example.track.repository.ProjectRepository;
import com.example.track.repository.SprintRepository;
import com.example.track.repository.UserStoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final UserStoryRepository userStoryRepository;
    private final ActivityLogService activityLogService;

    public List<SprintResponse> getProjectSprints(UUID projectId) {
        return sprintRepository.findByProjectIdOrderByStartDateDesc(projectId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SprintResponse createSprint(UUID projectId, CreateSprintRequest request, User currentUser) {
        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        Sprint sprint = Sprint.builder()
                .project(project)
                .name(request.getName())
                .goal(request.getGoal())
                .status(SprintStatus.PLANNING)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        Sprint saved = sprintRepository.save(sprint);

        activityLogService.logActivity("SPRINT", saved.getId(), currentUser, "created", null);

        return toResponse(saved);
    }

    public SprintResponse getSprint(UUID id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
        return toResponse(sprint);
    }

    @Transactional
    public SprintResponse updateSprint(UUID id, UpdateSprintRequest request) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        if (sprint.getStatus() == SprintStatus.COMPLETED) {
            throw new IllegalStateException("Cannot update a completed sprint");
        }

        if (request.getName() != null) {
            sprint.setName(request.getName());
        }
        if (request.getGoal() != null) {
            sprint.setGoal(request.getGoal());
        }
        if (request.getStartDate() != null) {
            sprint.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            sprint.setEndDate(request.getEndDate());
        }

        if (sprint.getEndDate().isBefore(sprint.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        sprint.setUpdatedAt(LocalDateTime.now());
        Sprint updated = sprintRepository.save(sprint);

        return toResponse(updated);
    }

    @Transactional
    public SprintResponse startSprint(UUID id, User currentUser) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        // Check if another sprint is already active
        if (sprintRepository.existsByProjectIdAndStatus(sprint.getProject().getId(), SprintStatus.ACTIVE)) {
            throw new IllegalStateException("Another sprint is already active in this project");
        }

        sprint.setStatus(SprintStatus.ACTIVE);
        sprint.setUpdatedAt(LocalDateTime.now());
        Sprint updated = sprintRepository.save(sprint);

        activityLogService.logActivity("SPRINT", updated.getId(), currentUser, "sprint_started", null);

        return toResponse(updated);
    }

    @Transactional
    public SprintResponse completeSprint(UUID id, CompleteSprintRequest request, User currentUser) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        // Get all stories in this sprint
        List<UserStory> stories = userStoryRepository.findBySprintIdOrderByPositionAsc(id);

        // Find unfinished tasks (stories with tasks that are not DONE)
        List<UserStory> storiesWithUnfinishedTasks = stories.stream()
                .filter(story -> story.getTasks().stream()
                        .anyMatch(task -> task.getStatus() != Task.TaskStatus.DONE))
                .collect(Collectors.toList());

        if (!storiesWithUnfinishedTasks.isEmpty()) {
            if ("MOVE_TO_BACKLOG".equals(request.getUnfinishedTaskAction())) {
                // Move stories to backlog
                storiesWithUnfinishedTasks.forEach(story -> story.setSprint(null));
                userStoryRepository.saveAll(storiesWithUnfinishedTasks);
            } else if ("MOVE_TO_NEXT_SPRINT".equals(request.getUnfinishedTaskAction())) {
                if (request.getNextSprintId() == null) {
                    throw new IllegalArgumentException("Next sprint ID is required for MOVE_TO_NEXT_SPRINT action");
                }
                Sprint nextSprint = sprintRepository.findById(request.getNextSprintId())
                        .orElseThrow(() -> new ResourceNotFoundException("Next sprint not found"));
                
                storiesWithUnfinishedTasks.forEach(story -> story.setSprint(nextSprint));
                userStoryRepository.saveAll(storiesWithUnfinishedTasks);
            }
        }

        sprint.setStatus(SprintStatus.COMPLETED);
        sprint.setUpdatedAt(LocalDateTime.now());
        Sprint updated = sprintRepository.save(sprint);

        activityLogService.logActivity("SPRINT", updated.getId(), currentUser, "sprint_completed", null);

        return toResponse(updated);
    }

    @Transactional
    public void deleteSprint(UUID id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        if (sprint.getStatus() != SprintStatus.PLANNING) {
            throw new IllegalStateException("Only PLANNING sprints can be deleted");
        }

        List<UserStory> stories = userStoryRepository.findBySprintIdOrderByPositionAsc(id);
        if (!stories.isEmpty()) {
            throw new IllegalStateException("Cannot delete sprint with assigned stories");
        }

        sprintRepository.delete(sprint);
    }

    private SprintResponse toResponse(Sprint sprint) {
        // Calculate task counts
        List<UserStory> stories = userStoryRepository.findBySprintIdOrderByPositionAsc(sprint.getId());
        
        int totalTasks = stories.stream()
                .mapToInt(story -> story.getTasks().size())
                .sum();
        
        int completedTasks = stories.stream()
                .flatMap(story -> story.getTasks().stream())
                .filter(task -> task.getStatus() == Task.TaskStatus.DONE)
                .mapToInt(task -> 1)
                .sum();
        
        double progress = totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0.0;

        return SprintResponse.builder()
                .id(sprint.getId())
                .name(sprint.getName())
                .goal(sprint.getGoal())
                .status(sprint.getStatus().name())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .projectId(sprint.getProject().getId())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .progress(progress)
                .createdAt(sprint.getCreatedAt())
                .updatedAt(sprint.getUpdatedAt())
                .build();
    }
}
