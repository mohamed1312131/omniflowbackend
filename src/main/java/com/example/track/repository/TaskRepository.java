package com.example.track.repository;

import com.example.track.domain.Task;
import com.example.track.domain.Task.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    
    List<Task> findByStoryIdOrderByPositionAsc(UUID storyId);
    
    List<Task> findByAssigneeIdOrderByPriorityDescDueDateAsc(UUID assigneeId);
    
    List<Task> findByStatus(TaskStatus status);
    
    List<Task> findByStatusNotAndDueDateBefore(TaskStatus status, LocalDate date);
    
    List<Task> findByStatusAndCompletedAtGreaterThanEqual(TaskStatus status, LocalDateTime dateTime);
    
    long countByStatusNot(TaskStatus status);
    
    long countByStatusNotAndDueDateBefore(TaskStatus status, LocalDate date);
}
