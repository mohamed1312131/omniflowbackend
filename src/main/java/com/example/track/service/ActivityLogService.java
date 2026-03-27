package com.example.track.service;

import com.example.track.domain.ActivityLog;
import com.example.track.domain.User;
import com.example.track.dto.response.ActivityLogResponse;
import com.example.track.dto.response.UserResponse;
import com.example.track.repository.ActivityLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void logActivity(String entityType, UUID entityId, User user, String action, Map<String, Object> details) {
        String detailsJson = null;
        if (details != null && !details.isEmpty()) {
            try {
                detailsJson = objectMapper.writeValueAsString(details);
            } catch (JsonProcessingException e) {
                // Log error but don't fail the operation
                detailsJson = details.toString();
            }
        }

        ActivityLog log = ActivityLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .user(user)
                .action(action)
                .details(detailsJson)
                .build();

        activityLogRepository.save(log);
    }

    public List<ActivityLogResponse> getActivityForEntity(String entityType, UUID entityId) {
        return activityLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ActivityLogResponse> getRecentActivity() {
        return activityLogRepository.findTop20ByOrderByCreatedAtDesc()
                .stream()
                .limit(20)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ActivityLogResponse toResponse(ActivityLog log) {
        return ActivityLogResponse.builder()
                .id(log.getId())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .action(log.getAction())
                .details(log.getDetails())
                .user(toUserResponse(log.getUser()))
                .createdAt(log.getCreatedAt())
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
