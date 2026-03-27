# Backend Refactoring Status - Module 1 API

## Completed
✅ New Flyway migrations (V1, V2, V3)
✅ Response wrapper (ApiResponse)
✅ Updated User entity (fullName, avatarUrl, isActive)
✅ Updated Project entity (status, createdBy)
✅ ProjectMember and ProjectMemberId entities
✅ New DTOs (UserResponse, LoginResponse, ProjectResponse, ProjectSummaryResponse)
✅ New Request DTOs (LoginRequest, ChangePasswordRequest, CreateUserRequest, UpdateUserRequest, CreateProjectRequest, UpdateProjectRequest, AddMemberRequest)
✅ Custom HexColor validator
✅ ProjectMemberRepository

## In Progress / Remaining

### 1. Update CustomUserDetailsService
- Change `getActive()` to `getIsActive()`

### 2. Refactor AuthService
- Update login to return LoginResponse with UserResponse
- Add changePassword method
- Update getCurrentUser to return UserResponse
- Remove register method (not in Module 1 spec)

### 3. Refactor AuthController
- Update to use ApiResponse wrapper
- Add PUT /api/auth/password endpoint
- Update GET /api/auth/me to return ApiResponse<UserResponse>
- Remove /register and /refresh endpoints

### 4. Create UserService
- getAllUsers() - admin only
- createUser() - admin only, check email uniqueness (409)
- getUserById() - admin: any user, developer: only self
- updateUser() - admin only, validate cannot deactivate last admin
- Helper: toUserResponse()

### 5. Create UserController
- GET /api/users - admin only
- POST /api/users - admin only
- GET /api/users/{id} - admin or self
- PUT /api/users/{id} - admin only
- All wrapped in ApiResponse

### 6. Refactor ProjectService
- Update to use new Project schema
- Add member management methods
- Update to return ProjectResponse/ProjectSummaryResponse
- Filter projects by user role (admin: all, developer: member of)

### 7. Refactor ProjectController
- Update all endpoints to use ApiResponse
- Add POST /api/projects/{id}/members
- Add DELETE /api/projects/{id}/members/{userId}
- Update authorization logic

### 8. Update SecurityConfig
- Change CORS to only allow http://localhost:5173
- Update public endpoints to only /api/auth/login
- Remove /register, /refresh from public

### 9. Update GlobalExceptionHandler
- Wrap all responses in ApiResponse
- Handle 409 Conflict for duplicate email
- Handle 403 Forbidden properly

### 10. Delete Old Code
- Remove Sprint, Story, Task, SubTask, Comment entities
- Remove related repositories
- Remove SprintService, StoryService, TaskService
- Remove SprintController, StoryController, TaskController
- Remove old DTOs
- Remove DataSeeder (will recreate simple version)

### 11. Configuration Files
- Create application.yml (replace application.properties)
- Create application-dev.yml
- Update JWT settings

### 12. Update README
- Document Module 1 API only
- Update test credentials
- Update setup instructions
