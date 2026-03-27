-- Test database connection and user lookup
-- Run this in pgAdmin to verify everything is correct

-- 1. Check if user exists
SELECT 'User exists:' as test, COUNT(*) as count
FROM users 
WHERE email = 'admin@taskflow.com';

-- 2. Check user details
SELECT 
    'User details:' as test,
    id,
    email, 
    full_name, 
    role, 
    is_active,
    LENGTH(password) as password_length,
    LEFT(password, 7) as password_start
FROM users 
WHERE email = 'admin@taskflow.com';

-- 3. Check if password hash is correct format
SELECT 
    'Password format check:' as test,
    CASE 
        WHEN password LIKE '$2a$10$%' THEN 'Valid BCrypt format'
        ELSE 'INVALID format'
    END as password_format,
    LENGTH(password) as length_should_be_60
FROM users 
WHERE email = 'admin@taskflow.com';

-- Expected results:
-- - User exists: count = 1
-- - is_active: true
-- - role: ADMIN
-- - password_length: 60
-- - password_start: $2a$10$
-- - password_format: Valid BCrypt format
