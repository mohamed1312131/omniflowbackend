-- Prepare database for Module 2 migrations
-- Run this in pgAdmin before starting the application

-- Step 1: Drop flyway schema history to start fresh
DROP TABLE IF EXISTS flyway_schema_history CASCADE;

-- Step 2: Verify existing Module 1 tables
SELECT 'Existing tables:' as info;
SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;

-- The application will now run Flyway with baseline-on-migrate=true
-- This will:
-- 1. Create flyway_schema_history table
-- 2. Mark V1, V2, V3 as baselined (since tables exist)
-- 3. Run V4, V5, V6, V7 (Module 2 migrations)
