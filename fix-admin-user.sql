-- Fix admin user password
-- Run this in pgAdmin Query Tool on trackdb

-- First, check current admin user
SELECT id, email, password, full_name, role, is_active 
FROM users 
WHERE email = 'admin@taskflow.com';

-- Update admin user with correct password hash
UPDATE users 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE email = 'admin@taskflow.com';

-- Verify update
SELECT id, email, full_name, role, is_active,
       substring(password, 1, 20) || '...' as password_hash
FROM users 
WHERE email = 'admin@taskflow.com';
