# TaskFlow API Testing Guide - Postman

## Quick Start

### 1. Import Postman Collection

Import the file: **`TaskFlow_Postman_Collection.json`** into Postman.

This collection includes all 14 Module 1 endpoints with automatic token management.

### 2. Start the Application

```bash
cd /Users/rouge/Desktop/track/trackBackEnd

# Clean Flyway history (since tables already exist)
# Run this SQL in pgAdmin first:
# DROP TABLE IF EXISTS flyway_schema_history CASCADE;

# Start application
mvn spring-boot:run
```

### 3. Test Endpoints in Order

## Step-by-Step Testing

### Step 1: Login (Get JWT Token)

**Endpoint:** `POST http://localhost:8080/api/auth/login`

**Body:**
```json
{
  "email": "admin@taskflow.com",
  "password": "Admin@123"
}
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400000,
    "user": {
      "id": "uuid-here",
      "email": "admin@taskflow.com",
      "fullName": "Admin",
      "avatarUrl": null,
      "role": "ADMIN",
      "isActive": true
    }
  }
}
```

**Important:** Copy the `token` value - you'll need it for all other requests!

---

### Step 2: Get Current User

**Endpoint:** `GET http://localhost:8080/api/auth/me`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "email": "admin@taskflow.com",
    "fullName": "Admin",
    "role": "ADMIN",
    "isActive": true
  }
}
```

---

### Step 3: Create a Developer User

**Endpoint:** `POST http://localhost:8080/api/users`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: application/json
```

**Body:**
```json
{
  "email": "developer@taskflow.com",
  "fullName": "John Developer",
  "password": "Dev@1234",
  "role": "DEVELOPER"
}
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "id": "new-user-uuid",
    "email": "developer@taskflow.com",
    "fullName": "John Developer",
    "role": "DEVELOPER",
    "isActive": true
  }
}
```

**Save the user ID** for later use!

---

### Step 4: Get All Users

**Endpoint:** `GET http://localhost:8080/api/users`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
```

**Expected Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "admin-uuid",
      "email": "admin@taskflow.com",
      "fullName": "Admin",
      "role": "ADMIN",
      "isActive": true
    },
    {
      "id": "dev-uuid",
      "email": "developer@taskflow.com",
      "fullName": "John Developer",
      "role": "DEVELOPER",
      "isActive": true
    }
  ]
}
```

---

### Step 5: Create a Project

**Endpoint:** `POST http://localhost:8080/api/projects`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: application/json
```

**Body:**
```json
{
  "name": "E-Commerce Platform",
  "description": "Building a modern e-commerce platform",
  "color": "#3B82F6",
  "memberIds": []
}
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "id": "project-uuid",
    "name": "E-Commerce Platform",
    "description": "Building a modern e-commerce platform",
    "color": "#3B82F6",
    "status": "ACTIVE",
    "memberCount": 1,
    "createdAt": "2024-03-14T...",
    "updatedAt": "2024-03-14T..."
  }
}
```

**Save the project ID!**

---

### Step 6: Get Project Details

**Endpoint:** `GET http://localhost:8080/api/projects/{projectId}`

Replace `{projectId}` with the ID from Step 5.

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "id": "project-uuid",
    "name": "E-Commerce Platform",
    "description": "Building a modern e-commerce platform",
    "color": "#3B82F6",
    "status": "ACTIVE",
    "createdBy": {
      "id": "admin-uuid",
      "email": "admin@taskflow.com",
      "fullName": "Admin",
      "role": "ADMIN"
    },
    "members": [
      {
        "id": "admin-uuid",
        "email": "admin@taskflow.com",
        "fullName": "Admin",
        "role": "ADMIN"
      }
    ],
    "createdAt": "2024-03-14T...",
    "updatedAt": "2024-03-14T..."
  }
}
```

---

### Step 7: Add Member to Project

**Endpoint:** `POST http://localhost:8080/api/projects/{projectId}/members`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: application/json
```

**Body:**
```json
{
  "userId": "developer-user-uuid-from-step-3"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Member added successfully"
}
```

---

## All Available Endpoints

### Authentication (3 endpoints)
1. ✅ `POST /api/auth/login` - Login
2. ✅ `GET /api/auth/me` - Get current user
3. ✅ `PUT /api/auth/password` - Change password

### Users (4 endpoints - Admin only)
4. ✅ `GET /api/users` - List all users
5. ✅ `POST /api/users` - Create user
6. ✅ `GET /api/users/{id}` - Get user by ID
7. ✅ `PUT /api/users/{id}` - Update user

### Projects (7 endpoints)
8. ✅ `GET /api/projects` - List projects
9. ✅ `POST /api/projects` - Create project (Admin)
10. ✅ `GET /api/projects/{id}` - Get project details
11. ✅ `PUT /api/projects/{id}` - Update project (Admin)
12. ✅ `DELETE /api/projects/{id}` - Delete project (Admin)
13. ✅ `POST /api/projects/{id}/members` - Add member (Admin)
14. ✅ `DELETE /api/projects/{id}/members/{userId}` - Remove member (Admin)

---

## Common Issues & Solutions

### Issue: "Invalid email or password"
**Solution:** Make sure the admin user password in database is exactly:
```
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
```
And password length is 60 characters.

### Issue: "401 Unauthorized"
**Solution:** 
1. Check token is included in Authorization header
2. Format: `Bearer YOUR_TOKEN_HERE` (with space after Bearer)
3. Token expires after 24 hours - login again

### Issue: "403 Forbidden"
**Solution:** 
- Endpoint requires ADMIN role
- Login with admin@taskflow.com

### Issue: Flyway migration error
**Solution:** Tables already exist. Run this in pgAdmin:
```sql
DROP TABLE IF EXISTS flyway_schema_history CASCADE;
```
Then restart the application.

---

## Testing Tips

1. **Use Postman Collection Variables:**
   - The collection automatically saves the token after login
   - Use `{{token}}` in Authorization headers
   - Use `{{baseUrl}}` for the API URL

2. **Test in Order:**
   - Always login first to get a token
   - Create users before adding them to projects
   - Create projects before testing project operations

3. **Check Response Status:**
   - `200 OK` - Success
   - `201 Created` - Resource created
   - `401 Unauthorized` - Missing/invalid token
   - `403 Forbidden` - Insufficient permissions
   - `404 Not Found` - Resource doesn't exist
   - `409 Conflict` - Duplicate (e.g., email already exists)

4. **Save IDs:**
   - Copy user IDs, project IDs from responses
   - You'll need them for update/delete operations

---

## Example: Complete Workflow

```bash
# 1. Login
POST /api/auth/login
→ Get token

# 2. Create developer user
POST /api/users
→ Get developer user ID

# 3. Create project
POST /api/projects
→ Get project ID

# 4. Add developer to project
POST /api/projects/{projectId}/members
Body: {"userId": "developer-id"}

# 5. View project with members
GET /api/projects/{projectId}
→ See both admin and developer as members

# 6. Update project status
PUT /api/projects/{projectId}
Body: {"status": "ON_HOLD"}

# 7. Test developer access
# Login as developer, try to view project
# Should work since they're a member
```

---

## Need Help?

- Check application logs for detailed error messages
- Verify database has correct data (use pgAdmin)
- Ensure PostgreSQL is running
- Check port 8080 is not in use by another application

**Application URL:** http://localhost:8080
**Database:** trackdb on localhost:5432
**Admin Credentials:** admin@taskflow.com / Admin@123
