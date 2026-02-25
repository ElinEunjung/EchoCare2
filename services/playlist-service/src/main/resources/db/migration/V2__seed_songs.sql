-- =============================================================================
-- V2: Seed Songs Data for Playlist Service
-- =============================================================================
-- 100 carefully selected songs for dementia care music therapy
-- Using fixed UUIDs to ensure idempotent migrations
-- Flyway ensures this only runs once via flyway_schema_history table
-- =============================================================================

-- CATEGORY 1: VERY CALMING (Energy 1.0-2.5, BPM 60-70)
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
('550e8400-e29b-41d4-a716-446655440010', 'Dream a Little Dream', 'The Mamas & The Papas', 1968, 68, 2.0),
('550e8400-e29b-41d4-a716-446655440011', 'Autumn Leaves', 'Ed Sheeran', 1945, 67, 1.8),
('550e8400-e29b-41d4-a716-446655440012', 'Misty', 'Erroll Garner', 1954, 64, 2.2),
('550e8400-e29b-41d4-a716-446655440013', 'The Very Thought of You', 'Billie Holiday', 1934, 66, 2.0),
('550e8400-e29b-41d4-a716-446655440014', 'All the Way', 'Frank Sinatra', 1957, 70, 2.3),
('550e8400-e29b-41d4-a716-446655440015', 'Cheek to Cheek', 'Fred Astaire', 1935, 69, 2.1);


-- CATEGORY 2: GENTLE CALMING (Energy 2.5-4.0, BPM 65-78)
INSERT INTO songs (id, title, artist, release_year, bpm, energy) VALUES
('550e8400-e29b-41d4-a716-446655440016', 'The Sound of Silence', 'Simon & Garfunkel', 1964, 64, 3.5),
('550e8400-e29b-41d4-a716-446655440017', 'Bridge Over Troubled Water', 'Simon & Garfunkel', 1970, 74, 3.5),
('550e8400-e29b-41d4-a716-446655440018', 'Imagine', 'John Lennon', 1971, 76, 3.5),
('550e8400-e29b-41d4-a716-446655440019', 'Unchained Melody', 'The Righteous Brothers', 1965, 69, 4.0),
('550e8400-e29b-41d4-a716-446655440020', 'Can''t Help Falling in Love', 'Elvis Presley', 1961, 68, 3.5),
('550e8400-e29b-41d4-a716-446655440021', 'A Whiter Shade of Pale', 'Procol Harum', 1967, 74, 4.0),
('550e8400-e29b-41d4-a716-446655440022', 'Wonderful Tonight', 'Eric Clapton', 1977, 72, 3.5),
('550e8400-e29b-41d4-a716-446655440023', 'The First Time Ever I Saw Your Face', 'Roberta Flack', 1972, 70, 3.0),
('550e8400-e29b-41d4-a716-446655440024', 'Your Song', 'Elton John', 1970, 76, 3.5),
('550e8400-e29b-41d4-a716-446655440025', 'Make You Feel My Love', 'Adele', 2008, 75, 4.0),
('550e8400-e29b-41d4-a716-446655440026', 'Clair de Lune', 'Claude Debussy', 1890, 60, 2.8),
('550e8400-e29b-41d4-a716-446655440027', 'Gymnopédie No. 1', 'Erik Satie', 1888, 62, 2.5),
('550e8400-e29b-41d4-a716-446655440028', 'Hallelujah', 'Leonard Cohen', 1984, 70, 3.8),
('550e8400-e29b-41d4-a716-446655440029', 'Someone Like You', 'Adele', 2011, 68, 3.5),
('550e8400-e29b-41d4-a716-446655440030', 'Mad World', 'Gary Jules', 2001, 65, 3.2);

-- CATEGORY 3: SOOTHING (Energy 4.0-5.0, BPM 72-85)
INSERT INTO songs (id, title, artist, release_year, bpm, energy) VALUES
('550e8400-e29b-41d4-a716-446655440031', 'Yesterday', 'The Beatles', 1965, 76, 4.5),
('550e8400-e29b-41d4-a716-446655440032', 'Let It Be', 'The Beatles', 1970, 73, 4.0),
('550e8400-e29b-41d4-a716-446655440033', 'Stand By Me', 'Ben E. King', 1961, 78, 5.0),
('550e8400-e29b-41d4-a716-446655440034', 'Lean on Me', 'Bill Withers', 1972, 80, 5.0),
('550e8400-e29b-41d4-a716-446655440035', 'Ain''t No Sunshine', 'Bill Withers', 1971, 82, 4.5),
('550e8400-e29b-41d4-a716-446655440036', 'You''ve Got a Friend', 'James Taylor', 1971, 78, 4.5),
('550e8400-e29b-41d4-a716-446655440037', 'Nowhere Man', 'The Beatles', 1965, 80, 4.5),
('550e8400-e29b-41d4-a716-446655440038', 'The Long and Winding Road', 'The Beatles', 1970, 72, 4.0),
('550e8400-e29b-41d4-a716-446655440039', 'Both Sides Now', 'Joni Mitchell', 1969, 75, 4.0),
('550e8400-e29b-41d4-a716-446655440040', 'Fire and Rain', 'James Taylor', 1970, 78, 4.5),
('550e8400-e29b-41d4-a716-446655440041', 'Michelle', 'The Beatles', 1966, 74, 4.2),
('550e8400-e29b-41d4-a716-446655440042', 'Blackbird', 'The Beatles', 1968, 75, 4.0),
('550e8400-e29b-41d4-a716-446655440043', 'While My Guitar Gently Weeps', 'The Beatles', 1968, 72, 4.3),
('550e8400-e29b-41d4-a716-446655440044', 'Ophelia', 'The Lumineers', 2012, 76, 4.8),
('550e8400-e29b-41d4-a716-446655440045', 'Skinny Love', 'Bon Iver', 2008, 80, 4.5),
('550e8400-e29b-41d4-a716-446655440046', 'Fast Car', 'Tracy Chapman', 1988, 74, 4.2),
('550e8400-e29b-41d4-a716-446655440047', 'The Scientist', 'Coldplay', 2002, 78, 4.0),
('550e8400-e29b-41d4-a716-446655440048', 'Fix You', 'Coldplay', 2005, 82, 4.8),
('550e8400-e29b-41d4-a716-446655440049', 'Chasing Cars', 'Snow Patrol', 2006, 80, 4.5),
('550e8400-e29b-41d4-a716-446655440050', 'Viva la Vida', 'Coldplay', 2008, 76, 4.8);

-- CATEGORY 4: UPLIFTING (Energy 5.5-6.5, BPM 90-110)
INSERT INTO songs (id, title, artist, release_year, bpm, energy) VALUES
('550e8400-e29b-41d4-a716-446655440051', 'Here Comes the Sun', 'The Beatles', 1969, 129, 6.5),
('550e8400-e29b-41d4-a716-446655440052', 'Hey Jude', 'The Beatles', 1968, 75, 6.0),
('550e8400-e29b-41d4-a716-446655440053', 'You Are the Sunshine of My Life', 'Stevie Wonder', 1973, 104, 6.0),
('550e8400-e29b-41d4-a716-446655440054', 'Piano Man', 'Billy Joel', 1973, 90, 5.5),
('550e8400-e29b-41d4-a716-446655440055', 'Take Me Home, Country Roads', 'John Denver', 1971, 98, 6.0),
('550e8400-e29b-41d4-a716-446655440056', 'Sweet Caroline', 'Neil Diamond', 1969, 112, 6.5),
('550e8400-e29b-41d4-a716-446655440057', 'Three Little Birds', 'Bob Marley', 1977, 76, 5.5),
('550e8400-e29b-41d4-a716-446655440058', 'Mr. Blue Sky', 'Electric Light Orchestra', 1977, 95, 6.5),
('550e8400-e29b-41d4-a716-446655440059', 'Don''t Worry Be Happy', 'Bobby McFerrin', 1988, 92, 6.0),
('550e8400-e29b-41d4-a716-446655440060', 'Beautiful Day', 'U2', 2000, 98, 6.5),
('550e8400-e29b-41d4-a716-446655440061', 'Sunrise Serenade', 'Glenn Miller', 1939, 100, 5.8),
('550e8400-e29b-41d4-a716-446655440101', 'Walking on Sunshine', 'Katrina & The Waves', 1985, 122, 6.2),
('550e8400-e29b-41d4-a716-446655440063', 'Good as It Gets', 'Sugarland', 2004, 100, 6.0),
('550e8400-e29b-41d4-a716-446655440064', 'Come Together', 'The Beatles', 1969, 84, 5.8),
('550e8400-e29b-41d4-a716-446655440065', 'Something', 'The Beatles', 1969, 88, 6.2);

-- CATEGORY 5: ENERGETIC (Energy 7.0-8.0, BPM 110-140)
INSERT INTO songs (id, title, artist, release_year, bpm, energy) VALUES
('550e8400-e29b-41d4-a716-446655440066', 'Dancing Queen', 'ABBA', 1976, 101, 7.5),
('550e8400-e29b-41d4-a716-446655440067', 'I Want to Hold Your Hand', 'The Beatles', 1963, 130, 7.0),
('550e8400-e29b-41d4-a716-446655440068', 'Brown Eyed Girl', 'Van Morrison', 1967, 140, 7.5),
('550e8400-e29b-41d4-a716-446655440069', 'Good Vibrations', 'The Beach Boys', 1966, 127, 7.5),
('550e8400-e29b-41d4-a716-446655440070', 'Build Me Up Buttercup', 'The Foundations', 1968, 138, 7.5),
('550e8400-e29b-41d4-a716-446655440071', 'Lovely Day', 'Bill Withers', 1977, 104, 7.0),
('550e8400-e29b-41d4-a716-446655440072', 'September', 'Earth, Wind & Fire', 1978, 126, 8.0),
('550e8400-e29b-41d4-a716-446655440073', 'Don''t Stop Believin''', 'Journey', 1981, 119, 7.0),
('550e8400-e29b-41d4-a716-446655440074', 'Walking on Sunshine', 'Katrina and the Waves', 1985, 122, 8.0),
('550e8400-e29b-41d4-a716-446655440075', 'I''m a Believer', 'The Monkees', 1966, 130, 7.5),
('550e8400-e29b-41d4-a716-446655440076', 'Daydream Believer', 'The Monkees', 1967, 128, 7.2),
('550e8400-e29b-41d4-a716-446655440077', 'Ob-La-Di, Ob-La-Da', 'The Beatles', 1968, 124, 7.8),
('550e8400-e29b-41d4-a716-446655440078', 'Come On Eileen', 'Dexys Midnight Runners', 1982, 120, 7.5),
('550e8400-e29b-41d4-a716-446655440079', 'Valerie', 'Amy Winehouse', 2007, 128, 7.8),
('550e8400-e29b-41d4-a716-446655440080', 'Mr. Brightside', 'The Killers', 2003, 122, 7.5),
('550e8400-e29b-41d4-a716-446655440081', 'Sex on Fire', 'Kings of Leon', 2010, 132, 7.8),
('550e8400-e29b-41d4-a716-446655440082', 'Wonderwall', 'Oasis', 1996, 110, 7.0),
('550e8400-e29b-41d4-a716-446655440083', 'Bitter Sweet Symphony', 'The Verve', 1997, 98, 7.2),
('550e8400-e29b-41d4-a716-446655440084', 'Last Nite', 'The Strokes', 2001, 110, 7.0),
('550e8400-e29b-41d4-a716-446655440085', 'Take Me Out', 'Franz Ferdinand', 2004, 164, 7.8);

-- CATEGORY 6: VERY ENERGETIC (Energy 8.5-10.0, BPM 140-180)
INSERT INTO songs (id, title, artist, release_year, bpm, energy) VALUES
('550e8400-e29b-41d4-a716-446655440086', 'Twist and Shout', 'The Beatles', 1963, 124, 8.0),
('550e8400-e29b-41d4-a716-446655440087', 'Twist', 'Chubby Checker', 1960, 160, 8.0),
('550e8400-e29b-41d4-a716-446655440088', 'Rock Around the Clock', 'Bill Haley & His Comets', 1954, 180, 9.0),
('550e8400-e29b-41d4-a716-446655440089', 'Johnny B. Goode', 'Chuck Berry', 1958, 166, 9.0),
('550e8400-e29b-41d4-a716-446655440090', 'Great Balls of Fire', 'Jerry Lee Lewis', 1957, 164, 9.5),
('550e8400-e29b-41d4-a716-446655440091', 'Shake It Off', 'Taylor Swift', 2014, 160, 9.0),
('550e8400-e29b-41d4-a716-446655440092', 'Happy', 'Pharrell Williams', 2013, 156, 8.5),
('550e8400-e29b-41d4-a716-446655440093', 'Uptown Funk', 'Bruno Mars', 2014, 115, 9.0),
('550e8400-e29b-41d4-a716-446655440094', 'I Gotta Feeling', 'Black Eyed Peas', 2009, 128, 8.0),
('550e8400-e29b-41d4-a716-446655440095', 'Can''t Stop the Feeling', 'Justin Timberlake', 2016, 113, 8.5),
('550e8400-e29b-41d4-a716-446655440096', 'Jailhouse Rock', 'Elvis Presley', 1957, 132, 8.8),
('550e8400-e29b-41d4-a716-446655440097', 'Let''s Twist Again', 'Chubby Checker', 1961, 158, 8.2),
('550e8400-e29b-41d4-a716-446655440098', 'The Locomotion', 'Little Eva', 1962, 126, 8.5),
('550e8400-e29b-41d4-a716-446655440099', 'Surfin'' U.S.A.', 'The Beach Boys', 1963, 134, 8.5),
('550e8400-e29b-41d4-a716-446655440100', 'California Girls', 'The Beach Boys', 1965, 152, 8.8);

