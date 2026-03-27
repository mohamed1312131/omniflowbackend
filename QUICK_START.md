# Quick Start Guide - TaskFlow Backend

## Error: `FATAL: role "trackuser" does not exist`

You need to set up PostgreSQL first. Follow these steps:

## Step 1: Setup PostgreSQL Database

### Option A: Using the setup script (Recommended)

```bash
# Run as PostgreSQL superuser
psql -U postgres -f setup-database.sql
```

### Option B: Manual setup

```bash
# Connect to PostgreSQL as superuser
psql -U postgres

# Then run these commands:
CREATE DATABASE trackdb;
CREATE USER trackuser WITH PASSWORD 'trackpass';
GRANT ALL PRIVILEGES ON DATABASE trackdb TO trackuser;

# Connect to the database
\c trackdb

# Grant schema privileges (required for PostgreSQL 15+)
GRANT ALL ON SCHEMA public TO trackuser;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO trackuser;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO trackuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO trackuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO trackuser;

# Exit
\q
```

### Option C: If you don't have PostgreSQL superuser access

If you can't access `postgres` user, try:

```bash
# On macOS (if installed via Homebrew)
psql postgres

# Or try your system username
psql -d postgres

# Then run the CREATE DATABASE and CREATE USER commands above
```

## Step 2: Verify Database Setup

```bash
# Test connection
psql -U trackuser -d trackdb

# If successful, you should see:
# trackdb=>

# Exit with \q
```

## Step 3: Run the Application

```bash
cd /Users/rouge/Desktop/track/trackBackEnd
mvn spring-boot:run
```

## Expected Output

When successful, you should see:

```
Flyway migration V1__create_users_table.sql completed successfully
Flyway migration V2__create_projects_table.sql completed successfully  
Flyway migration V3__seed_admin_user.sql completed successfully
Started TrackApplication in X.XXX seconds
```

## Step 4: Test the API

```bash
# Test login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@taskflow.com","password":"Admin@123"}'

# Expected response:
# {
#   "success": true,
#   "data": {
#     "token": "eyJhbGc...",
#     "expiresIn": 86400000,
#     "user": { ... }
#   }
# }
```

## Step 5: Access Swagger UI

Open in browser:
```
http://localhost:8080/swagger-ui.html
```

## Troubleshooting

### PostgreSQL not running
```bash
# macOS (Homebrew)
brew services start postgresql@14

# Or check status
brew services list
```

### Port 8080 already in use
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

### Flyway migration failed
```bash
# Check migration status
mvn flyway:info

# Repair if needed
mvn flyway:repair

# Or drop and recreate database
psql -U postgres
DROP DATABASE trackdb;
CREATE DATABASE trackdb;
GRANT ALL PRIVILEGES ON DATABASE trackdb TO trackuser;
```

## Default Credentials

**Admin User:**
- Email: `admin@taskflow.com`
- Password: `Admin@123`

Use these to login and create additional users.

## Next Steps

1. ✅ Database setup complete
2. ✅ Application running
3. ✅ Test login successful
4. 📝 Create additional users via POST /api/users
5. 📝 Create projects via POST /api/projects
6. 📝 Explore all endpoints in Swagger UI

## Need Help?

See the full README.md for complete documentation.
