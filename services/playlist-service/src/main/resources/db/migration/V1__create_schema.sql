-- =============================================================================
-- V1: Create Schema for Playlist Service
-- =============================================================================
-- This migration creates the initial database schema for playlist service
-- Tables: songs, playlists, playlist_songs (many-to-many join table)
-- =============================================================================

-- Create songs table
CREATE TABLE IF NOT EXISTS songs (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    artist VARCHAR(255) NOT NULL,
    release_year INTEGER NOT NULL,
    bpm DOUBLE PRECISION,
    energy DOUBLE PRECISION
);

-- Create playlists table
CREATE TABLE IF NOT EXISTS playlists (
    id UUID PRIMARY KEY,
    patient_profile_id UUID NOT NULL,
    care_need VARCHAR(255) NOT NULL,
    era VARCHAR(255),
    dementia_stage VARCHAR(255),
    created_at TIMESTAMP
);

-- Create playlist_songs join table (many-to-many relationship)
CREATE TABLE IF NOT EXISTS playlist_songs (
    playlist_id UUID NOT NULL,
    song_id UUID NOT NULL,
    PRIMARY KEY (playlist_id, song_id),
    CONSTRAINT fk_playlist FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE CASCADE,
    CONSTRAINT fk_song FOREIGN KEY (song_id) REFERENCES songs(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_songs_release_year ON songs(release_year);
CREATE INDEX IF NOT EXISTS idx_songs_bpm ON songs(bpm);
CREATE INDEX IF NOT EXISTS idx_songs_energy ON songs(energy);
CREATE INDEX IF NOT EXISTS idx_playlists_patient_profile_id ON playlists(patient_profile_id);
CREATE INDEX IF NOT EXISTS idx_playlist_songs_playlist_id ON playlist_songs(playlist_id);
CREATE INDEX IF NOT EXISTS idx_playlist_songs_song_id ON playlist_songs(song_id);

