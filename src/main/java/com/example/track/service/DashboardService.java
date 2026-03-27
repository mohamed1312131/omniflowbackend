package com.example.track.service;

import com.example.track.domain.Project;
import com.example.track.domain.Task;
import com.example.track.domain.User;
import com.example.track.dto.response.*;
import com.example.track.repository.ProjectRepository;
import com.example.track.repository.TaskRepository;
import com.example.track.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public DashboardStatsResponse getStats() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        long activeProjects = projectRepository.count(); // All projects are considered active
        long openTasks = taskRepository.countByStatusNot(Task.TaskStatus.DONE);
        long overdueTasks = taskRepository.countByStatusNotAndDueDateBefore(Task.TaskStatus.DONE, today);
        long completedThisWeek = taskRepository.findByStatusAndCompletedAtGreaterThanEqual(Task.TaskStatus.DONE, startOfWeek.atStartOfDay()).size();

        return DashboardStatsResponse.builder()
                .activeProjects(activeProjects)
                .openTasks(openTasks)
                .overdueTasks(overdueTasks)
                .completedThisWeek(completedThisWeek)
                .build();
    }

    public List<TaskResponse> getBlockedTasks() {
        return taskRepository.findByStatus(Task.TaskStatus.BLOCKED)
                .stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList());
    }

    public List<DeveloperPerformanceResponse> getPerformance() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate startOfLastWeek = startOfWeek.minusWeeks(1);

        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == User.Role.DEVELOPER)
                .map(user -> {
                    List<Task> userTasks = taskRepository.findByAssigneeIdOrderByPriorityDescDueDateAsc(user.getId());

                    long completedThisWeek = userTasks.stream()
                            .filter(t -> t.getStatus() == Task.TaskStatus.DONE && 
                                        t.getCompletedAt() != null &&
                                        !t.getCompletedAt().toLocalDate().isBefore(startOfWeek))
                            .count();

                    long completedLastWeek = userTasks.stream()
                            .filter(t -> t.getStatus() == Task.TaskStatus.DONE && 
                                        t.getCompletedAt() != null &&
                                        !t.getCompletedAt().toLocalDate().isBefore(startOfLastWeek) &&
                                        t.getCompletedAt().toLocalDate().isBefore(startOfWeek))
                            .count();

                    long assigned = userTasks.size();
                    long inProgress = userTasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.IN_PROGRESS).count();
                    long done = userTasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.DONE).count();
                    long blocked = userTasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.BLOCKED).count();
                    long overdue = userTasks.stream()
                            .filter(t -> t.getDueDate() != null && 
                                        t.getDueDate().isBefore(today) && 
                                        t.getStatus() != Task.TaskStatus.DONE)
                            .count();

                    return DeveloperPerformanceResponse.builder()
                            .user(toUserResponse(user))
                            .completedThisWeek(completedThisWeek)
                            .completedLastWeek(completedLastWeek)
                            .assigned(assigned)
                            .inProgress(inProgress)
                            .done(done)
                            .blocked(blocked)
                            .overdue(overdue)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<ActivityLogResponse> getRecentActivity() {
        return activityLogService.getRecentActivity();
    }

    private TaskResponse toTaskResponse(Task task) {
        LocalDate today = LocalDate.now();
        boolean isOverdue = task.getDueDate() != null && 
                           task.getDueDate().isBefore(today) && 
                           task.getStatus() != Task.TaskStatus.DONE;
        int daysOverdue = isOverdue ? (int) java.time.temporal.ChronoUnit.DAYS.between(task.getDueDate(), today) : 0;

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .blockedReason(task.getBlockedReason())
                .priority(task.getPriority().name())
                .assignee(task.getAssignee() != null ? toUserResponse(task.getAssignee()) : null)
                .dueDate(task.getDueDate())
                .storyId(task.getStory() != null ? task.getStory().getId() : null)
                .storyTitle(task.getStory() != null ? task.getStory().getTitle() : null)
                .projectId(task.getStory() != null && task.getStory().getProject() != null ? task.getStory().getProject().getId() : null)
                .projectName(task.getStory() != null && task.getStory().getProject() != null ? task.getStory().getProject().getName() : null)
                .isOverdue(isOverdue)
                .daysOverdue(daysOverdue)
                .completedAt(task.getCompletedAt())
                .subTasks(null)
                .subTasksCompleted(0)
                .subTasksTotal(0)
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
