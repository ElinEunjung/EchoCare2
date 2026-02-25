-- =============================================================================
-- V1: Create Schema for Profile Service
-- =============================================================================
-- This migration creates the initial database schema for profile service
-- Tables: caregivers, patient_profiles, favorite_artists, symptoms
-- =============================================================================

-- Create caregivers table
CREATE TABLE IF NOT EXISTS caregivers (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE
);

-- Create patient_profiles table
CREATE TABLE IF NOT EXISTS patient_profiles (
    id UUID PRIMARY KEY,
    patient_name VARCHAR(255) NOT NULL,
    era VARCHAR(255) NOT NULL,
    dementia_stage VARCHAR(255),
    caregiver_id UUID,
    CONSTRAINT fk_caregiver FOREIGN KEY (caregiver_id) REFERENCES caregivers(id) ON DELETE SET NULL
);

-- Create favorite_artists collection table
CREATE TABLE IF NOT EXISTS favorite_artists (
    patient_profile_id UUID NOT NULL,
    artist VARCHAR(255),
    CONSTRAINT fk_patient_favorite_artists FOREIGN KEY (patient_profile_id) REFERENCES patient_profiles(id) ON DELETE CASCADE
);

-- Create symptoms collection table
CREATE TABLE IF NOT EXISTS symptoms (
    patient_profile_id UUID NOT NULL,
    symptom VARCHAR(255) NOT NULL,
    CONSTRAINT fk_patient_symptoms FOREIGN KEY (patient_profile_id) REFERENCES patient_profiles(id) ON DELETE CASCADE
);


