# Module 2 Implementation - Sprint & Task Management

## ✅ Implementation Complete

Module 2 has been successfully implemented with all entities, services, and controllers for sprint and task management.

---

## 🗄️ Database Setup Required

Before starting the application, run this SQL in pgAdmin:

```sql
-- Drop flyway history to allow fresh migration
DROP TABLE IF EXISTS flyway_schema_history CASCADE;
```

Then start the application. Flyway will:
1. Baseline existing Module 1 tables (users, projects, project_members)
2. Run Module 2 migrations (V4-V7) to create new tables

---

## 📊 New Database Tables

### Module 2 Tables Created:
- **sprints** - Sprint management with PLANNING/ACTIVE/COMPLETED status
- **user_stories** - User stories linked to projects and sprints (nullable sprint = backlog)
- **tasks** - Tasks with TODO/IN_PROGRESS/IN_REVIEW/DONE/BLOCKED status
- **sub_tasks** - Subtasks for tasks with done/not done status
- **comments** - Comments on tasks
- **activity_logs** - Automatic activity tracking for all entities

---

## 🎯 New API Endpoints (30+ endpoints)

### Sprint Management (7 endpoints)

**GET** `/api/projects/{projectId}/sprints`
- Get all sprints for a project
- Returns: List of sprints with task counts and progress

**POST** `/api/projects/{projectId}/sprints` (Admin only)
- Create new sprint
- Body: `{ name, goal, startDate, endDate }`
- Validation: endDate must be after startDate

**GET** `/api/sprints/{id}`
- Get sprint details with computed metrics

**PUT** `/api/sprints/{id}` (Admin only)
- Update sprint (cannot update COMPLETED sprints)

**POST** `/api/sprints/{id}/start` (Admin only)
- Start sprint (sets status to ACTIVE)
- Business rule: Only ONE active sprint per project

**POST** `/api/sprints/{id}/complete` (Admin only)
- Complete sprint
- Body: `{ unfinishedTaskAction: "MOVE_TO_BACKLOG" | "MOVE_TO_NEXT_SPRINT", nextSprintId? }`
- Handles unfinished tasks automatically

**DELETE** `/api/sprints/{id}` (Admin only)
- Delete sprint (only PLANNING sprints without stories)

---

### User Story Management (5 endpoints)

**GET** `/api/projects/{projectId}/stories`
- Query params: `?sprintId={id}` or `?backlog=true`
- Returns stories with task counts and progress

**POST** `/api/projects/{projectId}/stories` (Admin only)
- Create story
- Body: `{ title, description, priority, sprintId? }`
- Null sprintId = backlog

**GET** `/api/stories/{id}`
- Get story with full task list

**PUT** `/api/stories/{id}` (Admin only)
- Update story, can move between sprints/backlog

**DELETE** `/api/stories/{id}` (Admin only)
- Delete story (cascades to tasks, subtasks, comments)

---

### Task Management (6 endpoints)

**GET** `/api/stories/{storyId}/tasks`
- Get all tasks for a story

**POST** `/api/stories/{storyId}/tasks` (Admin only)
- Create task
- Body: `{ title, description, priority, assigneeId?, dueDate?, status? }`

**GET** `/api/tasks/{id}`
- Get task with subtasks, comments, and activity

**PUT** `/api/tasks/{id}` ⚠️ **ROLE-BASED UPDATE**
- **Admin**: Can update ALL fields
- **Developer**: Can ONLY update `status` and `blockedReason`
- Auto-logs status changes
- Auto-sets `completedAt` when status = DONE
- Requires `blockedReason` when status = BLOCKED

**DELETE** `/api/tasks/{id}` (Admin only)

**GET** `/api/users/{userId}/tasks`
- Get all tasks assigned to user
- Developer: Can only query own tasks
- Admin: Can query any user's tasks
- Used for "My Tasks" screen

---

### SubTask Management (3 endpoints)

**POST** `/api/tasks/{taskId}/subtasks`
- Add subtask (Admin or assigned developer)
- Body: `{ title }`

**PUT** `/api/subtasks/{id}`
- Update subtask (Admin or assigned developer)
- Body: `{ title?, isDone? }`

**DELETE** `/api/subtasks/{id}`
- Delete subtask (Admin or assigned developer)

---

### Comment Management (3 endpoints)

**GET** `/api/tasks/{taskId}/comments`
- Get all comments for task (oldest first)

**POST** `/api/tasks/{taskId}/comments`
- Add comment (Admin or assigned developer)
- Body: `{ content }`
- Auto-logs "commented" activity

**DELETE** `/api/comments/{id}`
- Delete comment (Only author or admin)

---

### Activity Log (1 endpoint)

**GET** `/api/tasks/{taskId}/activity`
- Get activity log for task
- Returns: status changes, assignments, comments, etc.

---

### Dashboard Analytics (4 endpoints - Admin only)

**GET** `/api/dashboard/stats`
- Returns: `{ activeProjects, openTasks, overdueTasks, completedThisWeek }`

**GET** `/api/dashboard/blocked`
- Get all blocked tasks across projects
- Includes: blockedReason, days blocked, assignee

**GET** `/api/dashboard/performance`
- Per-developer performance metrics
- Returns: `{ user, completedThisWeek, completedLastWeek, assigned, inProgress, done, blocked, overdue }`

**GET** `/api/dashboard/activity`
- Last 20 activity logs across all projects

---

## 🔐 Role-Based Access Control

### Admin Permissions:
- ✅ Create/update/delete sprints, stories, tasks
- ✅ Start/complete sprints
- ✅ Assign tasks to developers
- ✅ Update ALL task fields
- ✅ View dashboard analytics
- ✅ View all users' tasks

### Developer Permissions:
- ✅ View sprints, stories, tasks
- ✅ Update task status and blockedReason ONLY
- ✅ Add/update/delete subtasks on assigned tasks
- ✅ Add/delete comments on assigned tasks
- ✅ View only own tasks via `/api/users/{userId}/tasks`
- ❌ Cannot create/delete sprints, stories, tasks
- ❌ Cannot update task title, priority, assignee, dueDate

---

## 🤖 Automatic Activity Logging

The system automatically logs these actions:
- **Task**: `created`, `status_changed`, `assigned`, `completed`, `commented`
- **Story**: `created`, `moved_to_sprint`
- **Sprint**: `created`, `sprint_started`, `sprint_completed`

All activity includes:
- User who performed action
- Timestamp
- Entity type and ID
- Action details (JSON)

---

## 📈 Computed Fields

### Sprint Response:
- `totalTasks` - Count of all tasks in sprint stories
- `completedTasks` - Count of DONE tasks
- `progress` - Percentage (completedTasks / totalTasks * 100)

### Story Response:
- `totalTasks` - Count of tasks in story
- `completedTasks` - Count of DONE tasks
- `progress` - Percentage

### Task Response:
- `isOverdue` - `dueDate < today AND status != DONE`
- `daysOverdue` - Days past due date
- `subTasksTotal` - Count of subtasks
- `subTasksCompleted` - Count of done subtasks

---

## 🔄 Business Rules

### Sprint Management:
1. Only ONE sprint can be ACTIVE per project at a time
2. Cannot update COMPLETED sprints
3. Can only delete PLANNING sprints without stories
4. Sprint completion handles unfinished tasks:
   - Move to backlog (set story.sprintId = null)
   - Move to next sprint (set story.sprintId = nextSprintId)

### Task Status Management:
1. When status → DONE: Auto-set `completedAt = now()`
2. When status → BLOCKED: Require `blockedReason`
3. When status changes FROM DONE: Clear `completedAt`
4. When status changes FROM BLOCKED: Clear `blockedReason`
5. All status changes are logged in activity_logs

### Developer Task Updates:
1. Developers can ONLY update `status` and `blockedReason`
2. Backend ignores all other fields when user role = DEVELOPER
3. This is enforced in `TaskService.updateTask()`

---

## 🧪 Testing Workflow

### 1. Setup Database
```sql
-- Run in pgAdmin
DROP TABLE IF EXISTS flyway_schema_history CASCADE;
```

### 2. Start Application
```bash
mvn spring-boot:run
```

### 3. Test in Swagger
Navigate to: `http://localhost:8080/swagger-ui.html`

### 4. Test Flow:
1. **Login** as admin (`admin@taskflow.com` / `Admin@123`)
2. **Create Project** (Module 1)
3. **Create Sprint** → POST `/api/projects/{projectId}/sprints`
4. **Create Story** → POST `/api/projects/{projectId}/stories`
5. **Create Task** → POST `/api/stories/{storyId}/tasks`
6. **Assign Developer** → PUT `/api/tasks/{id}` (set assigneeId)
7. **Developer Updates Status** → PUT `/api/tasks/{id}` (only status field)
8. **Add Subtasks** → POST `/api/tasks/{taskId}/subtasks`
9. **Add Comments** → POST `/api/tasks/{taskId}/comments`
10. **View Activity** → GET `/api/tasks/{taskId}/activity`
11. **Complete Sprint** → POST `/api/sprints/{id}/complete`
12. **View Dashboard** → GET `/api/dashboard/stats`

---

## 📝 Response Format

All endpoints use consistent `ApiResponse` wrapper:

```json
{
  "success": true,
  "data": { ... },
  "message": null
}
```

Error responses:
```json
{
  "success": false,
  "data": null,
  "message": "Error description"
}
```

---

## 🎨 Frontend Integration Notes

The frontend (Tracksystemfrontend) has mock UI for:
- Sprint boards with Kanban view
- Task detail panels with subtasks and comments
- Activity feeds
- Dashboard with performance metrics
- My Tasks view for developers

**Backend is now ready for frontend integration!**

All endpoints match the frontend's expected data shapes:
- Task status: `TODO`, `IN_PROGRESS`, `IN_REVIEW`, `DONE`, `BLOCKED`
- Priority: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- Sprint status: `PLANNING`, `ACTIVE`, `COMPLETED`

---

## 📊 Total Endpoint Count

**Module 1**: 15 endpoints (Auth, Users, Projects)
**Module 2**: 30+ endpoints (Sprints, Stories, Tasks, SubTasks, Comments, Activity, Dashboard)

**Total**: 45+ endpoints ready for testing! 🚀

---

## ⚠️ Important Notes

1. **Flyway**: Run `DROP TABLE flyway_schema_history` before first Module 2 startup
2. **Role-Based Updates**: Developer task updates are strictly enforced in backend
3. **Activity Logging**: Automatic - no manual logging needed
4. **Progress Calculation**: Computed on read, never stored
5. **Cascade Deletes**: Project → Sprints → Stories → Tasks → SubTasks/Comments

---

## 🎯 Next Steps

1. Run `prepare-module2-migration.sql` in pgAdmin
2. Start application: `mvn spring-boot:run`
3. Test all endpoints in Swagger UI
4. Verify role-based access control
5. Test sprint completion flow
6. Verify activity logging
7. Test dashboard analytics

Module 2 is complete and ready for production testing! ✅
