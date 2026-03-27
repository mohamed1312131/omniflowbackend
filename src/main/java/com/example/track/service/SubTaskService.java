package com.example.track.service;

import com.example.track.domain.SubTask;
import com.example.track.domain.Task;
import com.example.track.domain.User;
import com.example.track.dto.request.CreateSubTaskRequest;
import com.example.track.dto.request.UpdateSubTaskRequest;
import com.example.track.dto.response.SubTaskResponse;
import com.example.track.exception.ResourceNotFoundException;
import com.example.track.repository.SubTaskRepository;
import com.example.track.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubTaskService {

    private final SubTaskRepository subTaskRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public SubTaskResponse createSubTask(UUID taskId, CreateSubTaskRequest request, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // Check if user is admin or assigned to the task
        if (currentUser.getRole() != User.Role.ADMIN && 
            (task.getAssignee() == null || !task.getAssignee().getId().equals(currentUser.getId()))) {
            throw new IllegalArgumentException("Only admin or assigned developer can add subtasks");
        }

        SubTask subTask = SubTask.builder()
                .task(task)
                .title(request.getTitle())
                .isDone(false)
                .position(0)
                .build();

        SubTask saved = subTaskRepository.save(subTask);

        return toResponse(saved);
    }

    @Transactional
    public SubTaskResponse updateSubTask(UUID id, UpdateSubTaskRequest request, User currentUser) {
        SubTask subTask = subTaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubTask not found"));

        Task task = subTask.getTask();

        // Check if user is admin or assigned to the task
        if (currentUser.getRole() != User.Role.ADMIN && 
            (task.getAssignee() == null || !task.getAssignee().getId().equals(currentUser.getId()))) {
            throw new IllegalArgumentException("Only admin or assigned developer can update subtasks");
        }

        if (request.getTitle() != null) {
            subTask.setTitle(request.getTitle());
        }
        if (request.getIsDone() != null) {
            subTask.setIsDone(request.getIsDone());
        }

        SubTask updated = subTaskRepository.save(subTask);

        return toResponse(updated);
    }

    @Transactional
    public void deleteSubTask(UUID id, User currentUser) {
        SubTask subTask = subTaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubTask not found"));

        Task task = subTask.getTask();

        // Check if user is admin or assigned to the task
        if (currentUser.getRole() != User.Role.ADMIN && 
            (task.getAssignee() == null || !task.getAssignee().getId().equals(currentUser.getId()))) {
            throw new IllegalArgumentException("Only admin or assigned developer can delete subtasks");
        }

        subTaskRepository.delete(subTask);
    }

    private SubTaskResponse toResponse(SubTask subTask) {
        return SubTaskResponse.builder()
                .id(subTask.getId())
                .title(subTask.getTitle())
                .isDone(subTask.getIsDone())
                .position(subTask.getPosition())
                .build();
    }
}
