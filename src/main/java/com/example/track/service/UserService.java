package com.example.track.service;

import com.example.track.domain.User;
import com.example.track.dto.request.CreateUserRequest;
import com.example.track.dto.request.UpdateUserRequest;
import com.example.track.dto.response.UserResponse;
import com.example.track.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.debug("Creating user with email: {}", request.email());
        
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User.Role role;
        try {
            role = User.Role.valueOf(request.role().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role. Must be ADMIN or DEVELOPER");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(role)
                .isActive(true)
                .build();

        user = userRepository.save(user);
        log.info("Created user with id: {}", user.getId());
        
        return toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id, String currentUserEmail, boolean isAdmin) {
        log.debug("Fetching user with id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        // Developer can only view their own profile
        if (!isAdmin && !user.getEmail().equals(currentUserEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return toUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        log.debug("Updating user with id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        User.Role newRole;
        try {
            newRole = User.Role.valueOf(request.role().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role. Must be ADMIN or DEVELOPER");
        }

        // Check if trying to deactivate the last active admin
        if (Boolean.FALSE.equals(request.isActive()) && user.getRole() == User.Role.ADMIN) {
            long activeAdminCount = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == User.Role.ADMIN && u.getIsActive())
                    .count();
            
            if (activeAdminCount <= 1) {
                throw new IllegalArgumentException("Cannot deactivate the last active admin");
            }
        }

        user.setFullName(request.fullName());
        user.setRole(newRole);
        if (request.isActive() != null) {
            user.setIsActive(request.isActive());
        }

        user = userRepository.save(user);
        log.info("Updated user with id: {}", user.getId());
        
        return toUserResponse(user);
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .build();
    }
}
