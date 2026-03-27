-- Complete verification and recreation of admin user
-- Run this ENTIRE script in pgAdmin

-- Step 1: Check current state
SELECT '=== Current Admin User ===' as step;
SELECT 
    email, 
    full_name, 
    role, 
    is_active,
    LENGTH(password) as pwd_length,
    password
FROM users 
WHERE email = 'admin@taskflow.com';

-- Step 2: Delete and recreate with exact hash
DELETE FROM users WHERE email = 'admin@taskflow.com';

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

-- Step 3: Verify the new user
SELECT '=== New Admin User ===' as step;
SELECT 
    email, 
    full_name, 
    role, 
    is_active,
    LENGTH(password) as pwd_length_should_be_60,
    password as full_password_hash
FROM users 
WHERE email = 'admin@taskflow.com';
