-- Check current password hash in database
SELECT 
    email, 
    full_name, 
    role, 
    is_active,
    password as current_password_hash
FROM users 
WHERE email = 'admin@taskflow.com';

-- The correct bcrypt hash for "Admin@123" is:
-- $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

-- Update to correct password hash
UPDATE users 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE email = 'admin@taskflow.com';

-- Verify the update
SELECT 
    email, 
    full_name, 
    role, 
    is_active,
    LEFT(password, 29) as password_prefix
FROM users 
WHERE email = 'admin@taskflow.com';

-- Should show: $2a$10$N9qo8uLOickgx2ZMRZoMy
