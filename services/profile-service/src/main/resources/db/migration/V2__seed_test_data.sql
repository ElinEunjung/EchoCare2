-- =============================================================================
-- V2: Seed Test Data for Profile Service
-- =============================================================================
-- 10 test patient profiles with intuitive names
-- Using fixed UUIDs to ensure idempotent migrations
-- Flyway ensures this only runs once via flyway_schema_history table
-- =============================================================================

-- Insert 3 test caregivers (for future use, but profiles will work without them for MVP)
INSERT INTO caregivers (id, username, password_hash, email) VALUES
-- Note: In production, use BCrypt hashed passwords. These are placeholders for testing
('650e8400-e29b-41d4-a716-446655440001', 'sarah_wilson', '$2a$10$/9A5LkJI7mPug58JoT0Bs.UmGhYl4fF8qJb2FPtnWOEQoVEeUPjc2', 'sarah.wilson@echocare.com'),
('650e8400-e29b-41d4-a716-446655440002', 'john_martinez', '$2a$10$/9A5LkJI7mPug58JoT0Bs.UmGhYl4fF8qJb2FPtnWOEQoVEeUPjc2', 'john.martinez@echocare.com'),
('650e8400-e29b-41d4-a716-446655440003', 'emma_thompson', '$2a$10$/9A5LkJI7mPug58JoT0Bs.UmGhYl4fF8qJb2FPtnWOEQoVEeUPjc2', 'emma.thompson@echocare.com');

-- Insert 10 test patient profiles with intuitive names
-- Each represents common dementia patient scenarios

-- Profile 1: Margaret "Peggy" Anderson - 1950s music lover, mild dementia
INSERT INTO patient_profiles (id, patient_name, era, dementia_stage, caregiver_id) VALUES
('750e8400-e29b-41d4-a716-446655440001', 'Margaret Anderson', '1950-1960', 'mild', '650e8400-e29b-41d4-a716-446655440001');

INSERT INTO favorite_artists (patient_profile_id, artist) VALUES
('750e8400-e29b-41d4-a716-446655440001', 'Frank Sinatra'),
('750e8400-e29b-41d4-a716-446655440001', 'Nat King Cole'),
('750e8400-e29b-41d4-a716-446655440001', 'Ella Fitzgerald');

INSERT INTO symptoms (patient_profile_id, symptom) VALUES
('750e8400-e29b-41d4-a716-446655440001', 'memory_loss'),
('750e8400-e29b-41d4-a716-446655440001', 'mild_confusion');

-- Profile 2: Robert "Bob" Johnson - Beatles era, moderate dementia
INSERT INTO patient_profiles (id, patient_name, era, dementia_stage, caregiver_id) VALUES
('750e8400-e29b-41d4-a716-446655440002', 'Robert Johnson', '1960-1970', 'moderate', '650e8400-e29b-41d4-a716-446655440001');

INSERT INTO favorite_artists (patient_profile_id, artist) VALUES
('750e8400-e29b-41d4-a716-446655440002', 'The Beatles'),
('750e8400-e29b-41d4-a716-446655440002', 'The Rolling Stones'),
('750e8400-e29b-41d4-a716-446655440002', 'Elvis Presley');

INSERT INTO symptoms (patient_profile_id, symptom) VALUES
('750e8400-e29b-41d4-a716-446655440002', 'agitation'),
('750e8400-e29b-41d4-a716-446655440002', 'anxiety'),
('750e8400-e29b-41d4-a716-446655440002', 'wandering');

-- Profile 3: Dorothy "Dot" Williams - Classical music fan, severe dementia
INSERT INTO patient_profiles (id, patient_name, era, dementia_stage, caregiver_id) VALUES
('750e8400-e29b-41d4-a716-446655440003', 'Dorothy Williams', '1940-1950', 'severe', '650e8400-e29b-41d4-a716-446655440002');

INSERT INTO favorite_artists (patient_profile_id, artist) VALUES
('750e8400-e29b-41d4-a716-446655440003', 'Glenn Miller'),
('750e8400-e29b-41d4-a716-446655440003', 'Bing Crosby');

INSERT INTO symptoms (patient_profile_id, symptom) VALUES
('750e8400-e29b-41d4-a716-446655440003', 'severe_confusion'),
('750e8400-e29b-41d4-a716-446655440003', 'difficulty_speaking'),
('750e8400-e29b-41d4-a716-446655440003', 'depression');

-- Profile 4: James "Jim" Davis - Jazz enthusiast, mild dementia
INSERT INTO patient_profiles (id, patient_name, era, dementia_stage, caregiver_id) VALUES
('750e8400-e29b-41d4-a716-446655440004', 'James Davis', '1955-1965', 'mild', '650e8400-e29b-41d4-a716-446655440002');

INSERT INTO favorite_artists (patient_profile_id, artist) VALUES
('750e8400-e29b-41d4-a716-446655440004', 'Louis Armstrong'),
('750e8400-e29b-41d4-a716-446655440004', 'Duke Ellington'),
('750e8400-e29b-41d4-a716-446655440004', 'Billie Holiday');

INSERT INTO symptoms (patient_profile_id, symptom) VALUES
('750e8400-e29b-41d4-a716-446655440004', 'memory_loss'),
('750e8400-e29b-41d4-a716-446655440004', 'restlessness');

-- Profile 5: Elizabeth "Betty" Miller - Folk music lover, moderate dementia
INSERT INTO patient_profiles (id, patient_name, era, dementia_stage, caregiver_id) VALUES
('750e8400-e29b-41d4-a716-446655440005', 'Elizabeth Miller', '1965-1975', 'moderate', '650e8400-e29b-41d4-a716-446655440003');

INSERT INTO favorite_artists (patient_profile_id, artist) VALUES
('750e8400-e29b-41d4-a716-446655440005', 'Simon & Garfunkel'),
('750e8400-e29b-41d4-a716-446655440005', 'Bob Dylan'),
('750e8400-e29b-41d4-a716-446655440005', 'Joni Mitchell');

INSERT INTO symptoms (patient_profile_id, symptom) VALUES
('750e8400-e29b-41d4-a716-446655440005', 'anxiety'),
('750e8400-e29b-41d4-a716-446655440005', 'confusion'),
('750e8400-e29b-41d4-a716-446655440005', 'mood_swings');

-- Profile 6: Charles "Charlie" Brown - Country music fan, mild dementia
INSERT INTO patient_profiles (id, patient_name, era, dementia_stage, caregiver_id) VALUES
('750e8400-e29b-41d4-a716-446655440006', 'Charles Brown', '1970-1980', 'mild', '650e8400-e29b-41d4-a716-446655440003');

INSERT INTO favorite_artists (patient_profile_id, artist) VALUES
('750e8400-e29b-41d4-a716-446655440006', 'Johnny Cash'),
('750e8400-e29b-41d4-a716-446655440006', 'Willie Nelson'),
('750e8400-e29b-41d4-a716-446655440006', 'Dolly Parton');

INSERT INTO symptoms (patient_profile_id, symptom) VALUES
('750e8400-e29b-41d4-a716-446655440006', 'memory_loss'),
('750e8400-e29b-41d4-a716-446655440006', 'disorientation');

-- Profile 7: Helen Garcia - Motown fan, moderate dementia
INSERT INTO patient_profiles (id, patient_name, era, dementia_stage, caregiver_id) VALUES
('750e8400-e29b-41d4-a716-446655440007', 'Helen Garcia', '1960-1970', 'moderate', NULL);

INSERT INTO favorite_artists (patient_profile_id, artist) VALUES
('750e8400-e29b-41d4-a716-446655440007', 'Stevie Wonder'),
('750e8400-e29b-41d4-a716-446655440007', 'Diana Ross'),
('750e8400-e29b-41d4-a716-446655440007', 'Marvin Gaye');

INSERT INTO symptoms (patient_profile_id, symptom) VALUES
('750e8400-e29b-41d4-a716-446655440007', 'agitation'),
('750e8400-e29b-41d4-a716-446655440007', 'sleep_disturbance');

-- Profile 8: Arthur "Art" Martinez - Big Band era, severe dementia
INSERT INTO patient_profiles (id, patient_name, era, dementia_stage, caregiver_id) VALUES
('750e8400-e29b-41d4-a716-446655440008', 'Arthur Martinez', '1940-1950', 'severe', NULL);

INSERT INTO favorite_artists (patient_profile_id, artist) VALUES
('750e8400-e29b-41d4-a716-446655440008', 'Benny Goodman'),
('750e8400-e29b-41d4-a716-446655440008', 'Tommy Dorsey');

INSERT INTO symptoms (patient_profile_id, symptom) VALUES
('750e8400-e29b-41d4-a716-446655440008', 'severe_confusion'),
('750e8400-e29b-41d4-a716-446655440008', 'aggression'),
('750e8400-e29b-41d4-a716-446655440008', 'depression');

-- Profile 9: Patricia "Pat" Lee - Soft rock enthusiast, mild dementia
INSERT INTO patient_profiles (id, patient_name, era, dementia_stage, caregiver_id) VALUES
('750e8400-e29b-41d4-a716-446655440009', 'Patricia Lee', '1970-1980', 'mild', NULL);

INSERT INTO favorite_artists (patient_profile_id, artist) VALUES
('750e8400-e29b-41d4-a716-446655440009', 'Carole King'),
('750e8400-e29b-41d4-a716-446655440009', 'James Taylor'),
('750e8400-e29b-41d4-a716-446655440009', 'Fleetwood Mac');

INSERT INTO symptoms (patient_profile_id, symptom) VALUES
('750e8400-e29b-41d4-a716-446655440009', 'memory_loss'),
('750e8400-e29b-41d4-a716-446655440009', 'mild_anxiety');

-- Profile 10: William "Bill" Taylor - Oldies lover, moderate dementia
INSERT INTO patient_profiles (id, patient_name, era, dementia_stage, caregiver_id) VALUES
('750e8400-e29b-41d4-a716-446655440010', 'William Taylor', '1955-1965', 'moderate', NULL);

INSERT INTO favorite_artists (patient_profile_id, artist) VALUES
('750e8400-e29b-41d4-a716-446655440010', 'Chuck Berry'),
('750e8400-e29b-41d4-a716-446655440010', 'Buddy Holly'),
('750e8400-e29b-41d4-a716-446655440010', 'Little Richard');

INSERT INTO symptoms (patient_profile_id, symptom) VALUES
('750e8400-e29b-41d4-a716-446655440010', 'confusion'),
('750e8400-e29b-41d4-a716-446655440010', 'restlessness'),
('750e8400-e29b-41d4-a716-446655440010', 'anxiety');

