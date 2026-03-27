package com.example.track.service;

import com.example.track.domain.Comment;
import com.example.track.domain.Task;
import com.example.track.domain.User;
import com.example.track.dto.request.CreateCommentRequest;
import com.example.track.dto.response.CommentResponse;
import com.example.track.dto.response.UserResponse;
import com.example.track.exception.ResourceNotFoundException;
import com.example.track.repository.CommentRepository;
import com.example.track.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final ActivityLogService activityLogService;

    public List<CommentResponse> getTaskComments(UUID taskId) {
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse createComment(UUID taskId, CreateCommentRequest request, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // Check if user is admin or assigned to the task
        if (currentUser.getRole() != User.Role.ADMIN && 
            (task.getAssignee() == null || !task.getAssignee().getId().equals(currentUser.getId()))) {
            throw new IllegalArgumentException("Only admin or assigned developer can comment");
        }

        Comment comment = Comment.builder()
                .task(task)
                .user(currentUser)
                .content(request.getContent())
                .build();

        Comment saved = commentRepository.save(comment);

        activityLogService.logActivity("TASK", taskId, currentUser, "commented", null);

        return toResponse(saved);
    }

    @Transactional
    public void deleteComment(UUID id, User currentUser) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        // Only comment author or admin can delete
        if (currentUser.getRole() != User.Role.ADMIN && 
            !comment.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Only comment author or admin can delete comments");
        }

        commentRepository.delete(comment);
    }

    private CommentResponse toResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(toUserResponse(comment.getUser()))
                .createdAt(comment.getCreatedAt())
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
