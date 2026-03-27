-- Clean Database and Setup Fresh Admin User
-- Run this entire script in pgAdmin Query Tool on trackdb database

-- Step 1: Delete all existing data
DELETE FROM project_members;
DELETE FROM projects;
DELETE FROM users;

-- Step 2: Verify tables are empty
SELECT 'Users count:' as check, COUNT(*) as count FROM users;
SELECT 'Projects count:' as check, COUNT(*) as count FROM projects;
SELECT 'Project members count:' as check, COUNT(*) as count FROM project_members;

-- Step 3: You can now create your admin user manually
-- Use Postman to call POST /api/users endpoint (after you create first admin)
-- OR insert directly here:

-- Example: Insert admin user (you can modify the password hash)
-- Password: "Admin@123" 
-- Hash generated from: https://bcrypt-generator.com/ with 10 rounds
INSERT INTO users (email, password, full_name, role, is_active, created_at, updated_at)
VALUES (
    'admin@taskflow.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Admin',
    'ADMIN',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- OR use this hash for password "password" (easier to test)
-- INSERT INTO users (email, password, full_name, role, is_active, created_at, updated_at)
-- VALUES (
--     'admin@taskflow.com',
--     '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
--     'Admin',
--     'ADMIN',
--     true,
--     CURRENT_TIMESTAMP,
--     CURRENT_TIMESTAMP
-- );

-- Step 4: Verify admin user was created
SELECT 
    id,
    email,
    full_name,
    role,
    is_active,
    LENGTH(password) as password_length
FROM users 
WHERE email = 'admin@taskflow.com';

-- Step 5: Ready to test!
-- Use Postman to test: POST http://localhost:8080/api/auth/login
-- Body: {"email":"admin@taskflow.com","password":"Admin@123"}
-- OR: {"email":"admin@taskflow.com","password":"password"}
