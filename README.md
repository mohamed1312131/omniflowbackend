# TaskFlow Backend - Module 1

Spring Boot REST API for TaskFlow project management application.

## Tech Stack

- **Java 17**
- **Spring Boot 4.0.3**
- **PostgreSQL** - Database
- **Spring Security 6** - JWT Authentication
- **Spring Data JPA** - Data access
- **Flyway** - Database migrations
- **Lombok** - Boilerplate reduction
- **SpringDoc OpenAPI** - API documentation

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 14+

## Database Setup

1. **Install PostgreSQL** (if not already installed)

2. **Create database and user**:

```sql
CREATE DATABASE trackdb;
CREATE USER trackuser WITH PASSWORD 'trackpass';
GRANT ALL PRIVILEGES ON DATABASE trackdb TO trackuser;
```

3. **Verify connection**:
```bash
psql -U trackuser -d trackdb
```

## Running the Application

### 1. Build the project

```bash
mvn clean install -DskipTests
```

### 2. Run the application

```bash
# Production mode
mvn spring-boot:run

# Development mode (with debug logging)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The application will start on `http://localhost:8080`

### 3. Verify startup

- Flyway migrations V1, V2, V3 will run automatically
- Admin user will be seeded: `admin@taskflow.com` / `Admin@123`
- Check logs for successful startup

## Default Credentials

**Admin User:**
- Email: `admin@taskflow.com`
- Password: `Admin@123`

Use these credentials to login and create additional users.

## API Documentation

### Swagger UI
Access interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON
```
http://localhost:8080/api-docs
```

## Module 1 API Endpoints

### Authentication

#### POST /api/auth/login
Login with email and password.

**Request:**
```json
{
  "email": "admin@taskflow.com",
  "password": "Admin@123"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400000,
    "user": {
      "id": "uuid",
      "email": "admin@taskflow.com",
      "fullName": "Admin",
      "avatarUrl": null,
      "role": "ADMIN",
      "isActive": true
    }
  }
}
```

#### GET /api/auth/me
Get current user information (requires JWT token).

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "email": "admin@taskflow.com",
    "fullName": "Admin",
    "avatarUrl": null,
    "role": "ADMIN",
    "isActive": true
  }
}
```

#### PUT /api/auth/password
Change password (requires JWT token).

**Request:**
```json
{
  "currentPassword": "Admin@123",
  "newPassword": "NewPassword123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Password updated successfully"
}
```

### Users (Admin Only)

#### GET /api/users
Get all users (Admin only).

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "email": "admin@taskflow.com",
      "fullName": "Admin",
      "avatarUrl": null,
      "role": "ADMIN",
      "isActive": true
    }
  ]
}
```

#### POST /api/users
Create a new user (Admin only).

**Request:**
```json
{
  "email": "dev@taskflow.com",
  "fullName": "Developer User",
  "password": "Dev@1234",
  "role": "DEVELOPER"
}
```

**Validation:**
- Email must be unique (409 Conflict if duplicate)
- Password minimum 8 characters
- Role must be "ADMIN" or "DEVELOPER"

#### GET /api/users/{id}
Get user by ID.
- **Admin**: Can view any user
- **Developer**: Can only view their own profile (403 otherwise)

#### PUT /api/users/{id}
Update user (Admin only).

**Request:**
```json
{
  "fullName": "Updated Name",
  "role": "ADMIN",
  "isActive": false
}
```

**Business Rule:** Cannot deactivate the last active admin.

### Projects

#### GET /api/projects
Get all projects.
- **Admin**: Returns all projects
- **Developer**: Returns only projects they are a member of

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "name": "E-Commerce Platform",
      "description": "Building a modern e-commerce platform",
      "color": "#3B82F6",
      "status": "ACTIVE",
      "memberCount": 3,
      "createdAt": "2024-03-14T10:00:00",
      "updatedAt": "2024-03-14T10:00:00"
    }
  ]
}
```

#### POST /api/projects
Create a new project (Admin only).

**Request:**
```json
{
  "name": "New Project",
  "description": "Project description",
  "color": "#3B82F6",
  "memberIds": ["uuid1", "uuid2"]
}
```

**Business Rules:**
- Creator is automatically added as a member
- Color must be valid hex format (#RRGGBB)
- Members must be active users

#### GET /api/projects/{id}
Get project by ID with full member list.
- **Admin**: Can view any project
- **Developer**: Must be a member (403 otherwise)

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "name": "E-Commerce Platform",
    "description": "Building a modern e-commerce platform",
    "color": "#3B82F6",
    "status": "ACTIVE",
    "createdBy": {
      "id": "uuid",
      "email": "admin@taskflow.com",
      "fullName": "Admin",
      "role": "ADMIN"
    },
    "members": [
      {
        "id": "uuid",
        "email": "dev@taskflow.com",
        "fullName": "Developer",
        "role": "DEVELOPER"
      }
    ],
    "createdAt": "2024-03-14T10:00:00",
    "updatedAt": "2024-03-14T10:00:00"
  }
}
```

#### PUT /api/projects/{id}
Update project (Admin only).

**Request:**
```json
{
  "name": "Updated Name",
  "description": "Updated description",
  "color": "#22C55E",
  "status": "ON_HOLD"
}
```

**Valid statuses:** ACTIVE, ON_HOLD, COMPLETED, ARCHIVED

#### DELETE /api/projects/{id}
Delete project (Admin only). Cascades to project members.

#### POST /api/projects/{id}/members
Add member to project (Admin only).

**Request:**
```json
{
  "userId": "uuid"
}
```

**Business Rules:**
- User must exist and be active
- Cannot add duplicate members

#### DELETE /api/projects/{id}/members/{userId}
Remove member from project (Admin only).

**Business Rule:** Cannot remove the project creator.

## Authentication

All endpoints except `POST /api/auth/login` require JWT authentication.

**Include token in requests:**
```
Authorization: Bearer <your_jwt_token>
```

**Token expiration:** 24 hours (86400000 ms)

## Response Format

All API responses follow this structure:

**Success:**
```json
{
  "success": true,
  "message": "optional message",
  "data": { ... }
}
```

**Error:**
```json
{
  "success": false,
  "message": "error message"
}
```

**Validation Error:**
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "email": "Email is required",
    "password": "Password must be at least 8 characters"
  }
}
```

## HTTP Status Codes

- `200 OK` - Success
- `201 Created` - Resource created
- `204 No Content` - Success with no response body
- `400 Bad Request` - Validation error
- `401 Unauthorized` - Invalid or missing JWT token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Duplicate resource (e.g., email already exists)
- `500 Internal Server Error` - Server error

## Configuration

### Database
Edit `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/trackdb
    username: trackuser
    password: trackpass
```

### JWT Secret
**IMPORTANT:** Change the JWT secret in production!
```yaml
jwt:
  secret: your-secret-key-change-this-in-production-min-256-bits-long-for-hs256-algorithm
  access-token-expiration: 86400000
```

### CORS
Configured to allow requests from:
- `http://localhost:5173` (Vite frontend dev server)

Update `CorsConfig.java` to add additional origins.

## Development

### Enable Debug Logging

Run with dev profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

This enables:
- SQL query logging
- Formatted SQL
- Debug logging for security and application code

### Database Migrations

Flyway migrations are in `src/main/resources/db/migration/`:
- `V1__create_users_table.sql` - Users table
- `V2__create_projects_table.sql` - Projects and project_members tables
- `V3__seed_admin_user.sql` - Seed admin user

**Important:** JPA is configured with `ddl-auto: validate` - schema changes must be done via Flyway migrations.

## Testing the API

### Using curl

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@taskflow.com","password":"Admin@123"}'
```

**Get current user:**
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <your_token>"
```

**Create project:**
```bash
curl -X POST http://localhost:8080/api/projects \
  -H "Authorization: Bearer <your_token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Project","description":"Test","color":"#3B82F6"}'
```

### Using Swagger UI

1. Navigate to `http://localhost:8080/swagger-ui.html`
2. Click "Authorize" button
3. Enter: `Bearer <your_token>`
4. Test endpoints interactively

## Troubleshooting

### Database Connection Failed
```bash
# Check PostgreSQL is running
pg_isready

# Verify database exists
psql -U trackuser -d trackdb
```

### Flyway Migration Failed
```bash
# Check migration status
mvn flyway:info

# Repair if needed
mvn flyway:repair
```

### JWT Token Invalid
- Ensure token is not expired (24 hour lifetime)
- Check Authorization header format: `Bearer <token>`
- Verify JWT secret matches in application.yml

### Build Errors
```bash
# Clean and rebuild
mvn clean install -DskipTests

# Check Java version
java -version  # Should be 17+
```

## Project Structure

```
src/main/java/com/example/track/
├── config/              # Configuration classes
│   ├── CorsConfig.java
│   └── OpenApiConfig.java
├── controller/          # REST controllers
│   ├── AuthController.java
│   ├── ProjectController.java
│   └── UserController.java
├── domain/              # JPA entities
│   ├── Project.java
│   ├── ProjectMember.java
│   ├── ProjectMemberId.java
│   └── User.java
├── dto/                 # Data Transfer Objects
│   ├── request/
│   └── response/
├── exception/           # Exception handling
│   └── GlobalExceptionHandler.java
├── repository/          # Spring Data repositories
│   ├── ProjectMemberRepository.java
│   ├── ProjectRepository.java
│   └── UserRepository.java
├── security/            # Security configuration
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtUtil.java
│   └── SecurityConfig.java
├── service/             # Business logic
│   ├── AuthService.java
│   ├── ProjectService.java
│   └── UserService.java
└── validation/          # Custom validators
    ├── HexColor.java
    └── HexColorValidator.java

src/main/resources/
├── db/migration/        # Flyway migrations
│   ├── V1__create_users_table.sql
│   ├── V2__create_projects_table.sql
│   └── V3__seed_admin_user.sql
├── application.yml      # Main configuration
└── application-dev.yml  # Development configuration
```

## Roadmap

### Module 1 (Current) ✅
- User authentication and management
- Project management
- Team member management

### Module 2 (Coming Next)
- Sprints
- User Stories
- Tasks and Subtasks
- Comments and Activity Log

## License

Proprietary - All rights reserved

## Support

For issues or questions, please contact the development team.
