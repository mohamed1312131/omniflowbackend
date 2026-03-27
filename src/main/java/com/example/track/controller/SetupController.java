package com.example.track.controller;

import com.example.track.domain.User;
import com.example.track.dto.response.ApiResponse;
import com.example.track.dto.response.UserResponse;
import com.example.track.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class SetupController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/create-admin")
    public ResponseEntity<ApiResponse<UserResponse>> createAdmin(@RequestBody CreateAdminRequest request) {
        // Check if any admin already exists
        boolean adminExists = userRepository.findAll().stream()
                .anyMatch(user -> user.getRole() == User.Role.ADMIN);
        
        if (adminExists) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Admin user already exists. Use /api/users to create additional users."));
        }

        // Create admin user
        User admin = new User();
        admin.setEmail(request.email());
        admin.setPassword(passwordEncoder.encode(request.password()));
        admin.setFullName("Admin");
        admin.setRole(User.Role.ADMIN);
        admin.setIsActive(true);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());

        User savedAdmin = userRepository.save(admin);

        UserResponse response = UserResponse.builder()
                .id(savedAdmin.getId())
                .email(savedAdmin.getEmail())
                .fullName(savedAdmin.getFullName())
                .avatarUrl(savedAdmin.getAvatarUrl())
                .role(savedAdmin.getRole().name())
                .isActive(savedAdmin.getIsActive())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    public record CreateAdminRequest(String email, String password) {}
}
