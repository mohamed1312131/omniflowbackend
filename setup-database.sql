-- TaskFlow Database Setup Script
-- Run this script as PostgreSQL superuser (postgres)

-- Create database
CREATE DATABASE trackdb;

-- Create user
CREATE USER trackuser WITH PASSWORD 'trackpass';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE trackdb TO trackuser;

-- Connect to trackdb and grant schema privileges
\c trackdb

-- Grant schema privileges (PostgreSQL 15+ requires this)
GRANT ALL ON SCHEMA public TO trackuser;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO trackuser;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO trackuser;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO trackuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO trackuser;

-- Verify setup
\du trackuser
\l trackdb
