
-- =============================================================================
-- SEED DATA for Profile Service Database
-- =============================================================================
-- Purpose: Initialize the database with sample patient profiles and caregivers
-- Usage: Automatically loaded by Spring Boot on application startup
-- =============================================================================

-- Sample Patient Profiles
-- These patients have different musical preferences and dementia stages
-- Used by playlist-service to generate personalized music therapy playlists

INSERT INTO patient_profiles (id, patient_name, era, dementia_stage) VALUES
-- Patient 1: Elderly patient who loves 1950s music
('550e8400-e29b-41d4-a716-446655440000', 'John Smith', '1950-1960', 'MILD'),

-- Patient 2: Moderate dementia, loves 1960s rock
('550e8400-e29b-41d4-a716-446655440001', 'Mary Johnson', '1960-1970', 'MODERATE'),

-- Patient 3: Severe stage, prefers calming 1940s music
('550e8400-e29b-41d4-a716-446655440002', 'Robert Williams', '1940-1950', 'SEVERE'),

-- Patient 4: Mild stage, enjoys 1970s disco and soul
('550e8400-e29b-41d4-a716-446655440003', 'Patricia Brown', '1970-1980', 'MILD'),

-- Patient 5: Moderate stage, fan of 1980s pop
('550e8400-e29b-41d4-a716-446655440004', 'James Davis', '1980-1990', 'MODERATE');

-- Favorite Artists for each patient
INSERT INTO favorite_artists (patient_profile_id, artist) VALUES
-- Patient 1: John Smith (1950s)
('550e8400-e29b-41d4-a716-446655440000', 'Frank Sinatra'),
('550e8400-e29b-41d4-a716-446655440000', 'Nat King Cole'),
('550e8400-e29b-41d4-a716-446655440000', 'Ella Fitzgerald'),

-- Patient 2: Mary Johnson (1960s)
('550e8400-e29b-41d4-a716-446655440001', 'The Beatles'),
('550e8400-e29b-41d4-a716-446655440001', 'Elvis Presley'),
('550e8400-e29b-41d4-a716-446655440001', 'Simon & Garfunkel'),

-- Patient 3: Robert Williams (1940s)
('550e8400-e29b-41d4-a716-446655440002', 'Louis Armstrong'),
('550e8400-e29b-41d4-a716-446655440002', 'Billie Holiday'),
('550e8400-e29b-41d4-a716-446655440002', 'Duke Ellington'),

-- Patient 4: Patricia Brown (1970s)
('550e8400-e29b-41d4-a716-446655440003', 'Stevie Wonder'),
('550e8400-e29b-41d4-a716-446655440003', 'ABBA'),
('550e8400-e29b-41d4-a716-446655440003', 'Earth Wind & Fire'),

-- Patient 5: James Davis (1980s)
('550e8400-e29b-41d4-a716-446655440004', 'Michael Jackson'),
('550e8400-e29b-41d4-a716-446655440004', 'Madonna'),
('550e8400-e29b-41d4-a716-446655440004', 'Queen');

-- Symptoms for each patient
INSERT INTO symptoms (patient_profile_id, symptom) VALUES
-- Patient 1: John Smith
('550e8400-e29b-41d4-a716-446655440000', 'Memory loss'),
('550e8400-e29b-41d4-a716-446655440000', 'Confusion'),

-- Patient 2: Mary Johnson
('550e8400-e29b-41d4-a716-446655440001', 'Agitation'),
('550e8400-e29b-41d4-a716-446655440001', 'Anxiety'),

-- Patient 3: Robert Williams
('550e8400-e29b-41d4-a716-446655440002', 'Severe memory loss'),
('550e8400-e29b-41d4-a716-446655440002', 'Agitation'),
('550e8400-e29b-41d4-a716-446655440002', 'Depression'),

-- Patient 4: Patricia Brown
('550e8400-e29b-41d4-a716-446655440003', 'Mild forgetfulness'),
('550e8400-e29b-41d4-a716-446655440003', 'Low energy'),

-- Patient 5: James Davis
('550e8400-e29b-41d4-a716-446655440004', 'Confusion'),
('550e8400-e29b-41d4-a716-446655440004', 'Mood swings');
