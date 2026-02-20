-- Test data for EchoCare Profile Service
-- This file contains sample data for testing the patient profile creation endpoint

-- 1. Create a test caregiver (required for patient profiles)
INSERT INTO caregivers (id, username, password, email)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'john_doe', 'hashed_password_123', 'john@example.com')
ON CONFLICT (id) DO NOTHING;

-- Optional: Add more caregivers for testing
INSERT INTO caregivers (id, username, password, email)
VALUES ('550e8400-e29b-41d4-a716-446655440001', 'jane_smith', 'hashed_password_456', 'jane@example.com')
ON CONFLICT (id) DO NOTHING;

-- Verify caregivers were inserted
SELECT * FROM caregivers;

