package com.example.track.service;

import com.example.track.domain.Project;
import com.example.track.domain.ProjectMember;
import com.example.track.domain.User;
import com.example.track.dto.request.AddMemberRequest;
import com.example.track.dto.request.CreateProjectRequest;
import com.example.track.dto.request.UpdateProjectRequest;
import com.example.track.dto.response.ProjectProgressResponse;
import com.example.track.dto.response.ProjectResponse;
import com.example.track.dto.response.ProjectSummaryResponse;
import com.example.track.dto.response.SprintResponse;
import com.example.track.dto.response.UserResponse;
import com.example.track.repository.ProjectMemberRepository;
import com.example.track.repository.ProjectRepository;
import com.example.track.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final SprintService sprintService;

    @Transactional(readOnly = true)
    public List<ProjectSummaryResponse> getAllProjects(String currentUserEmail, boolean isAdmin) {
        log.debug("Fetching projects for user: {}, isAdmin: {}", currentUserEmail, isAdmin);
        
        if (isAdmin) {
            return projectRepository.findAll().stream()
                    .map(this::toProjectSummaryResponse)
                    .collect(Collectors.toList());
        } else {
            // Developer: only projects they're a member of
            User currentUser = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            List<ProjectMember> memberships = projectMemberRepository.findByUserId(currentUser.getId());
            return memberships.stream()
                    .map(pm -> projectRepository.findById(pm.getProjectId()))
                    .filter(opt -> opt.isPresent())
                    .map(opt -> toProjectSummaryResponse(opt.get()))
                    .collect(Collectors.toList());
        }
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, String creatorEmail) {
        log.debug("Creating project: {}", request.name());
        
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new IllegalArgumentException("Creator user not found"));

        // Validate color if provided
        String color = request.color() != null ? request.color() : "#3B82F6";

        Project project = Project.builder()
                .name(request.name())
                .description(request.description())
                .color(color)
                .status(Project.Status.ACTIVE)
                .createdBy(creator)
                .build();

        project = projectRepository.save(project);
        log.info("Created project with id: {}", project.getId());

        // Auto-add creator as member
        ProjectMember creatorMember = new ProjectMember();
        creatorMember.setProjectId(project.getId());
        creatorMember.setUserId(creator.getId());
        projectMemberRepository.save(creatorMember);
        log.debug("Added creator as project member");

        // Add additional members if provided
        if (request.memberIds() != null && !request.memberIds().isEmpty()) {
            for (UUID memberId : request.memberIds()) {
                if (!memberId.equals(creator.getId())) {
                    User member = userRepository.findById(memberId)
                            .orElseThrow(() -> new IllegalArgumentException("Member user not found: " + memberId));
                    
                    if (!member.getIsActive()) {
                        throw new IllegalArgumentException("Cannot add inactive user as member: " + memberId);
                    }

                    ProjectMember pm = new ProjectMember();
                    pm.setProjectId(project.getId());
                    pm.setUserId(memberId);
                    projectMemberRepository.save(pm);
                }
            }
        }

        return toProjectResponse(project);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID id, String currentUserEmail, boolean isAdmin) {
        log.debug("Fetching project with id: {}", id);
        
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + id));

        // Developer must be a member
        if (!isAdmin) {
            User currentUser = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            if (!projectMemberRepository.existsByProjectIdAndUserId(id, currentUser.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }
        }

        return toProjectResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID id, UpdateProjectRequest request) {
        log.debug("Updating project with id: {}", id);
        
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + id));

        Project.Status status;
        try {
            status = Project.Status.valueOf(request.status().toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status. Must be ACTIVE, ON_HOLD, COMPLETED, or ARCHIVED");
        }

        project.setName(request.name());
        project.setDescription(request.description());
        project.setColor(request.color());
        project.setStatus(status);

        project = projectRepository.save(project);
        log.info("Updated project with id: {}", project.getId());
        
        return toProjectResponse(project);
    }

    @Transactional
    public void deleteProject(UUID id) {
        log.debug("Deleting project with id: {}", id);
        
        if (!projectRepository.existsById(id)) {
            throw new IllegalArgumentException("Project not found with id: " + id);
        }
        
        projectRepository.deleteById(id);
        log.info("Deleted project with id: {}", id);
    }

    @Transactional
    public void addMember(UUID projectId, AddMemberRequest request) {
        log.debug("Adding member {} to project {}", request.userId(), projectId);
        
        if (!projectRepository.existsById(projectId)) {
            throw new IllegalArgumentException("Project not found with id: " + projectId);
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + request.userId()));

        if (!user.getIsActive()) {
            throw new IllegalArgumentException("Cannot add inactive user as member");
        }

        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, request.userId())) {
            throw new IllegalArgumentException("User is already a member of this project");
        }

        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(request.userId());
        projectMemberRepository.save(member);
        
        log.info("Added member {} to project {}", request.userId(), projectId);
    }

    @Transactional
    public void removeMember(UUID projectId, UUID userId) {
        log.debug("Removing member {} from project {}", userId, projectId);
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

        // Cannot remove the project creator
        if (project.getCreatedBy().getId().equals(userId)) {
            throw new IllegalArgumentException("Cannot remove the project creator");
        }
        
        // Use project variable to avoid warning
        log.debug("Project {} validated for member removal", project.getId());

        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new IllegalArgumentException("User is not a member of this project");
        }

        projectMemberRepository.deleteByProjectIdAndUserId(projectId, userId);
        log.info("Removed member {} from project {}", userId, projectId);
    }

    private ProjectSummaryResponse toProjectSummaryResponse(Project project) {
        long memberCount = projectMemberRepository.countByProjectId(project.getId());
        
        return ProjectSummaryResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .color(project.getColor())
                .status(project.getStatus().name())
                .memberCount((int) memberCount)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    private ProjectResponse toProjectResponse(Project project) {
        List<ProjectMember> members = projectMemberRepository.findByProjectId(project.getId());
        List<UserResponse> memberResponses = members.stream()
                .map(pm -> userRepository.findById(pm.getUserId()))
                .filter(opt -> opt.isPresent())
                .map(opt -> toUserResponse(opt.get()))
                .collect(Collectors.toList());

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .color(project.getColor())
                .status(project.getStatus().name())
                .createdBy(toUserResponse(project.getCreatedBy()))
                .members(memberResponses)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public ProjectProgressResponse getProjectProgress(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        
        List<SprintResponse> sprints = sprintService.getProjectSprints(projectId);
        
        // Calculate overall project progress based on completed tasks across all sprints
        long totalTasks = sprints.stream().mapToLong(SprintResponse::getTotalTasks).sum();
        long completedTasks = sprints.stream().mapToLong(SprintResponse::getCompletedTasks).sum();
        double progress = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0.0;
        
        return ProjectProgressResponse.builder()
                .projectProgress(progress)
                .sprints(sprints)
                .build();
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
