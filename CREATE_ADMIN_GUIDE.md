# Create Admin User - Quick Guide

## ✅ New Setup Endpoint Available

I've added a special endpoint to create the first admin user without authentication.

---

## 🚀 Method 1: Using Postman (Recommended)

### Step 1: Clean Database (Optional)

If you want to start fresh, run this in pgAdmin:

```sql
DELETE FROM project_members;
DELETE FROM projects;
DELETE FROM users;
```

### Step 2: Create Admin User

**Endpoint:** `POST http://localhost:8080/api/setup/create-admin`

**Headers:**
```
Content-Type: application/json
```

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
    "id": "uuid-here",
    "email": "admin@taskflow.com",
    "fullName": "Admin",
    "avatarUrl": null,
    "role": "ADMIN",
    "isActive": true
  }
}
```

### Step 3: Login

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
      "id": "uuid",
      "email": "admin@taskflow.com",
      "fullName": "Admin",
      "role": "ADMIN",
      "isActive": true
    }
  }
}
```

### Step 4: Test Other Endpoints

Use the token from login to test:
- GET `/api/auth/me`
- GET `/api/users`
- POST `/api/users` (create developer)
- POST `/api/projects`
- And more...

---

## 🔒 Security Note

The `/api/setup/create-admin` endpoint:
- ✅ Only works if NO admin exists
- ✅ Automatically hashes the password with BCrypt
- ✅ Creates user with ADMIN role
- ❌ Returns error if admin already exists

Once you create the first admin, use the normal `/api/users` endpoint (requires authentication) to create additional users.

---

## 🧪 Method 2: Using cURL

```bash
# Create admin
curl -X POST http://localhost:8080/api/setup/create-admin \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@taskflow.com","password":"Admin@123"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@taskflow.com","password":"Admin@123"}'
```

---

## ❌ Error Handling

### "Admin user already exists"
```json
{
  "success": false,
  "message": "Admin user already exists. Use /api/users to create additional users."
}
```

**Solution:** Admin already created. Just login with existing credentials.

### "Invalid email or password" (on login)
**Solution:** 
1. Verify email is correct
2. Verify password matches what you used in create-admin
3. Check database: `SELECT email, role FROM users WHERE email = 'admin@taskflow.com';`

---

## 📋 Complete Workflow

1. **Clean database** (optional): Delete all users
2. **Create admin**: POST `/api/setup/create-admin`
3. **Login**: POST `/api/auth/login` → Get token
4. **Create developer**: POST `/api/users` with token
5. **Create project**: POST `/api/projects` with token
6. **Add members**: POST `/api/projects/{id}/members` with token

---

## 🎯 Quick Test Commands

```bash
# 1. Create admin
curl -X POST http://localhost:8080/api/setup/create-admin \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@taskflow.com","password":"Admin@123"}'

# 2. Login and save token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@taskflow.com","password":"Admin@123"}' | jq -r '.data.token')

# 3. Get current user
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $TOKEN"

# 4. List all users
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN"
```

---

## ✅ Application Status

- **URL:** http://localhost:8080
- **Status:** ✅ Running
- **Spring Boot:** 3.2.5
- **Database:** trackdb (PostgreSQL)
- **Flyway:** Disabled (manual setup)

All 15 endpoints ready:
- 3 Auth endpoints
- 4 User endpoints  
- 7 Project endpoints
- **1 Setup endpoint** (new!)

Happy testing! 🚀
