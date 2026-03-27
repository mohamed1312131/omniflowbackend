-- COMPLETE DIAGNOSTIC AND FIX SCRIPT
-- Run this entire script in pgAdmin Query Tool on trackdb database

-- Step 1: Check if admin user exists and view all details
SELECT '=== STEP 1: Current Admin User ===' as step;
SELECT 
    id,
    email, 
    full_name, 
    role, 
    is_active,
    created_at,
    password as password_hash
FROM users 
WHERE email = 'admin@taskflow.com';

-- Step 2: Delete existing admin user (if exists)
SELECT '=== STEP 2: Deleting old admin user ===' as step;
DELETE FROM users WHERE email = 'admin@taskflow.com';

-- Step 3: Insert fresh admin user with correct password
SELECT '=== STEP 3: Creating fresh admin user ===' as step;
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

-- Step 4: Verify the new admin user
SELECT '=== STEP 4: Verification ===' as step;
SELECT 
    id,
    email, 
    full_name, 
    role, 
    is_active,
    created_at,
    LEFT(password, 29) as password_prefix,
    LENGTH(password) as password_length
FROM users 
WHERE email = 'admin@taskflow.com';

-- Step 5: Check all tables exist
SELECT '=== STEP 5: All Tables ===' as step;
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;

SELECT '=== COMPLETE! Now restart the application ===' as final_message;
