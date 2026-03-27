-- Fix Flyway Schema History
-- Run this in pgAdmin Query Tool on trackdb database

-- Drop Flyway history table (since we manually created tables)
DROP TABLE IF EXISTS flyway_schema_history CASCADE;

-- Verify all tables exist
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;

-- Should show: project_members, projects, users

-- Verify admin user exists with correct password
SELECT 
    email,
    full_name,
    role,
    is_active,
    LENGTH(password) as password_length_should_be_60
FROM users 
WHERE email = 'admin@taskflow.com';

-- If password length is not 60, run this:
-- UPDATE users 
-- SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
-- WHERE email = 'admin@taskflow.com';
