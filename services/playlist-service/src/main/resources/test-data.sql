-- =============================================================================
-- SEED DATA for Playlist Service Database
-- =============================================================================
-- Purpose: Initialize the database with sample songs for playlist generation
-- Usage: Automatically loaded by Spring Boot on application startup
--
-- 60 carefully selected songs designed for dementia care music therapy
-- Covers all care needs: stress_relief, activity_support, calming_agitation,
-- easing_depression, reducing_anxiety
-- Spans eras: 1940s-2010s with focus on familiar classics (1950s-1980s)
-- BPM range: 60-180, Energy range: 1.0-10.0
-- =============================================================================

-- CATEGORY 1: VERY CALMING (Energy 1.0-2.5, BPM 60-70)
-- Perfect for: reducing_anxiety, calming_agitation (severe stage)
INSERT INTO songs (id, title, artist, release_year, bpm, energy) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'What a Wonderful World', 'Louis Armstrong', 1967, 72, 2.0),
('550e8400-e29b-41d4-a716-446655440002', 'Moon River', 'Andy Williams', 1961, 68, 2.5),
('550e8400-e29b-41d4-a716-446655440003', 'Unforgettable', 'Nat King Cole', 1951, 66, 2.0),
('550e8400-e29b-41d4-a716-446655440004', 'The Way You Look Tonight', 'Frank Sinatra', 1964, 70, 2.5),
('550e8400-e29b-41d4-a716-446655440005', 'Somewhere Over the Rainbow', 'Judy Garland', 1939, 68, 2.0),
('550e8400-e29b-41d4-a716-446655440006', 'Edelweiss', 'Julie Andrews', 1965, 66, 1.5),
('550e8400-e29b-41d4-a716-446655440007', 'Georgia on My Mind', 'Ray Charles', 1960, 72, 2.5),
('550e8400-e29b-41d4-a716-446655440008', 'At Last', 'Etta James', 1960, 65, 2.0),
('550e8400-e29b-41d4-a716-446655440009', 'Fly Me to the Moon', 'Frank Sinatra', 1964, 70, 2.5),
('550e8400-e29b-41d4-a716-446655440010', 'Dream a Little Dream', 'The Mamas & The Papas', 1968, 68, 2.0);

-- CATEGORY 2: GENTLE CALMING (Energy 2.5-4.0, BPM 65-78)
-- Perfect for: reducing_anxiety, stress_relief (moderate stage)
INSERT INTO songs (id, title, artist, release_year, bpm, energy) VALUES
('550e8400-e29b-41d4-a716-446655440011', 'The Sound of Silence', 'Simon & Garfunkel', 1964, 64, 3.5),
('550e8400-e29b-41d4-a716-446655440012', 'Bridge Over Troubled Water', 'Simon & Garfunkel', 1970, 74, 3.5),
('550e8400-e29b-41d4-a716-446655440013', 'Imagine', 'John Lennon', 1971, 76, 3.5),
('550e8400-e29b-41d4-a716-446655440014', 'Unchained Melody', 'The Righteous Brothers', 1965, 69, 4.0),
('550e8400-e29b-41d4-a716-446655440015', 'Can''t Help Falling in Love', 'Elvis Presley', 1961, 68, 3.5),
('550e8400-e29b-41d4-a716-446655440016', 'A Whiter Shade of Pale', 'Procol Harum', 1967, 74, 4.0),
('550e8400-e29b-41d4-a716-446655440017', 'Wonderful Tonight', 'Eric Clapton', 1977, 72, 3.5),
('550e8400-e29b-41d4-a716-446655440018', 'The First Time Ever I Saw Your Face', 'Roberta Flack', 1972, 70, 3.0),
('550e8400-e29b-41d4-a716-446655440019', 'Your Song', 'Elton John', 1970, 76, 3.5),
('550e8400-e29b-41d4-a716-446655440020', 'Make You Feel My Love', 'Adele', 2008, 75, 4.0);

-- CATEGORY 3: SOOTHING (Energy 4.0-5.0, BPM 72-85)
-- Perfect for: stress_relief, easing_depression (mild to moderate)
INSERT INTO songs (id, title, artist, release_year, bpm, energy) VALUES
('550e8400-e29b-41d4-a716-446655440021', 'Yesterday', 'The Beatles', 1965, 76, 4.5),
('550e8400-e29b-41d4-a716-446655440022', 'Let It Be', 'The Beatles', 1970, 73, 4.0),
('550e8400-e29b-41d4-a716-446655440023', 'Stand By Me', 'Ben E. King', 1961, 78, 5.0),
('550e8400-e29b-41d4-a716-446655440024', 'Lean on Me', 'Bill Withers', 1972, 80, 5.0),
('550e8400-e29b-41d4-a716-446655440025', 'Ain''t No Sunshine', 'Bill Withers', 1971, 82, 4.5),
('550e8400-e29b-41d4-a716-446655440026', 'You''ve Got a Friend', 'James Taylor', 1971, 78, 4.5),
('550e8400-e29b-41d4-a716-446655440027', 'Nowhere Man', 'The Beatles', 1965, 80, 4.5),
('550e8400-e29b-41d4-a716-446655440028', 'The Long and Winding Road', 'The Beatles', 1970, 72, 4.0),
('550e8400-e29b-41d4-a716-446655440029', 'Both Sides Now', 'Joni Mitchell', 1969, 75, 4.0),
('550e8400-e29b-41d4-a716-446655440030', 'Fire and Rain', 'James Taylor', 1970, 78, 4.5);

-- CATEGORY 4: UPLIFTING (Energy 5.5-6.5, BPM 90-110)
-- Perfect for: easing_depression, stress_relief (mild stage)
INSERT INTO songs (id, title, artist, release_year, bpm, energy) VALUES
('550e8400-e29b-41d4-a716-446655440031', 'Here Comes the Sun', 'The Beatles', 1969, 129, 6.5),
('550e8400-e29b-41d4-a716-446655440032', 'Hey Jude', 'The Beatles', 1968, 75, 6.0),
('550e8400-e29b-41d4-a716-446655440033', 'You Are the Sunshine of My Life', 'Stevie Wonder', 1973, 104, 6.0),
('550e8400-e29b-41d4-a716-446655440034', 'Piano Man', 'Billy Joel', 1973, 90, 5.5),
('550e8400-e29b-41d4-a716-446655440035', 'Take Me Home, Country Roads', 'John Denver', 1971, 98, 6.0),
('550e8400-e29b-41d4-a716-446655440036', 'Sweet Caroline', 'Neil Diamond', 1969, 112, 6.5),
('550e8400-e29b-41d4-a716-446655440037', 'Three Little Birds', 'Bob Marley', 1977, 76, 5.5),
('550e8400-e29b-41d4-a716-446655440038', 'Mr. Blue Sky', 'Electric Light Orchestra', 1977, 95, 6.5),
('550e8400-e29b-41d4-a716-446655440039', 'Don''t Worry Be Happy', 'Bobby McFerrin', 1988, 92, 6.0),
('550e8400-e29b-41d4-a716-446655440040', 'Beautiful Day', 'U2', 2000, 98, 6.5);

-- CATEGORY 5: ENERGETIC (Energy 7.0-8.0, BPM 110-140)
-- Perfect for: activity_support (mild to moderate)
INSERT INTO songs (id, title, artist, release_year, bpm, energy) VALUES
('550e8400-e29b-41d4-a716-446655440041', 'Dancing Queen', 'ABBA', 1976, 101, 7.5),
('550e8400-e29b-41d4-a716-446655440042', 'I Want to Hold Your Hand', 'The Beatles', 1963, 130, 7.0),
('550e8400-e29b-41d4-a716-446655440043', 'Brown Eyed Girl', 'Van Morrison', 1967, 140, 7.5),
('550e8400-e29b-41d4-a716-446655440044', 'Good Vibrations', 'The Beach Boys', 1966, 127, 7.5),
('550e8400-e29b-41d4-a716-446655440045', 'Build Me Up Buttercup', 'The Foundations', 1968, 138, 7.5),
('550e8400-e29b-41d4-a716-446655440046', 'Lovely Day', 'Bill Withers', 1977, 104, 7.0),
('550e8400-e29b-41d4-a716-446655440047', 'September', 'Earth, Wind & Fire', 1978, 126, 8.0),
('550e8400-e29b-41d4-a716-446655440048', 'Don''t Stop Believin''', 'Journey', 1981, 119, 7.0),
('550e8400-e29b-41d4-a716-446655440049', 'Walking on Sunshine', 'Katrina and the Waves', 1985, 122, 8.0),
('550e8400-e29b-41d4-a716-446655440050', 'I''m a Believer', 'The Monkees', 1966, 130, 7.5);

-- CATEGORY 6: VERY ENERGETIC (Energy 8.5-10.0, BPM 140-180)
-- Perfect for: activity_support (mild stage only)
INSERT INTO songs (id, title, artist, release_year, bpm, energy) VALUES
('550e8400-e29b-41d4-a716-446655440051', 'Twist and Shout', 'The Beatles', 1963, 124, 8.0),
('550e8400-e29b-41d4-a716-446655440052', 'Twist', 'Chubby Checker', 1960, 160, 8.0),
('550e8400-e29b-41d4-a716-446655440053', 'Rock Around the Clock', 'Bill Haley & His Comets', 1954, 180, 9.0),
('550e8400-e29b-41d4-a716-446655440054', 'Johnny B. Goode', 'Chuck Berry', 1958, 166, 9.0),
('550e8400-e29b-41d4-a716-446655440055', 'Great Balls of Fire', 'Jerry Lee Lewis', 1957, 164, 9.5),
('550e8400-e29b-41d4-a716-446655440056', 'Shake It Off', 'Taylor Swift', 2014, 160, 9.0),
('550e8400-e29b-41d4-a716-446655440057', 'Happy', 'Pharrell Williams', 2013, 156, 8.5),
('550e8400-e29b-41d4-a716-446655440058', 'Uptown Funk', 'Bruno Mars', 2014, 115, 9.0),
('550e8400-e29b-41d4-a716-446655440059', 'I Gotta Feeling', 'Black Eyed Peas', 2009, 128, 8.0),
('550e8400-e29b-41d4-a716-446655440060', 'Can''t Stop the Feeling', 'Justin Timberlake', 2016, 113, 8.5);

