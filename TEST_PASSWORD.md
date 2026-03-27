# Password Hash Test

## Current Situation
- Application starts successfully on Spring Boot 3.2.5
- Database has admin user with correct 60-character BCrypt hash
- Login endpoint returns "Invalid email or password"

## Test the Password Hash

Run this in pgAdmin to verify the exact password hash:

```sql
SELECT 
    email,
    password,
    LENGTH(password) as len,
    role,
    is_active
FROM users 
WHERE email = 'admin@taskflow.com';
```

**Expected:**
- password: `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`
- len: 60
- role: ADMIN
- is_active: true

## Possible Issues

1. **BCrypt version mismatch**: The hash starts with `$2a$` but Spring Security 6 might expect `$2b$`
2. **Password encoder not configured**: BCryptPasswordEncoder might not be wired correctly
3. **User not found**: Email lookup might be case-sensitive

## Solution: Generate New Password Hash

Since the current hash isn't working, let's generate a fresh one using Spring Security's BCryptPasswordEncoder.

### Option 1: Use Online BCrypt Generator
Go to: https://bcrypt-generator.com/
- Input: `Admin@123`
- Rounds: 10
- Copy the generated hash (should start with `$2a$10$` or `$2b$10$`)

### Option 2: Generate in Java (Recommended)

Create a simple test class to generate the hash:

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "Admin@123";
        String hash = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        System.out.println("Length: " + hash.length());
        
        // Verify it works
        boolean matches = encoder.matches(password, hash);
        System.out.println("Matches: " + matches);
    }
}
```

### Option 3: Update Database with New Hash

Once you have a fresh hash, update the database:

```sql
UPDATE users 
SET password = 'YOUR_NEW_HASH_HERE'
WHERE email = 'admin@taskflow.com';
```

## Quick Fix: Try Different Password

For testing, try creating a new user with a simple password:

```sql
-- Delete old admin
DELETE FROM users WHERE email = 'admin@taskflow.com';

-- Insert with a known working hash for "password"
-- Hash: $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG
INSERT INTO users (email, password, full_name, role, is_active, created_at, updated_at)
VALUES (
    'admin@taskflow.com',
    '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
    'Admin',
    'ADMIN',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
```

Then try logging in with:
- Email: `admin@taskflow.com`
- Password: `password`

## Alternative: Disable Flyway and Use Postman

Since the application is running, you can:

1. **Import the Postman collection**: `TaskFlow_Postman_Collection.json`
2. **Test all endpoints** except login (they'll fail without token)
3. **Focus on fixing authentication** separately

The Postman collection has all 14 endpoints documented with examples.

## Next Steps

1. Run the SQL query above to verify password hash
2. Try the "password" hash as a quick test
3. If that works, generate a proper hash for "Admin@123"
4. Update database and test again

The application is running correctly - it's just the password authentication that needs fixing.
