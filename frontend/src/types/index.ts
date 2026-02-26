// Auth Types
export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
  role: 'CAREGIVER' | 'ADMIN';
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  userId: string;
  email: string;
  role: string;
}

// Profile Types
export interface PatientProfile {
  id: string;
  name: string;
  dateOfBirth: string;
  musicalEra: string;
  favoriteArtists: string[];
  symptoms: string[];
  caregiverId: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreatePatientProfileRequest {
  name: string;
  dateOfBirth: string;
  musicalEra: string;
  favoriteArtists: string[];
  symptoms: string[];
  caregiverId: string;
}

// Caregiver Types
export interface Caregiver {
  id: string;
  fullName: string;
  email: string;
  role: string;
  createdAt: string;
}

// Playlist Types
export interface Playlist {
  id: string;
  profileId: string;
  profileName: string;
  generatedAt: string;
  songs: Song[];
}

export interface Song {
  id: string;
  title: string;
  artist: string;
  year?: number;
  genre?: string;
  duration?: number;
}

export interface GeneratePlaylistRequest {
  profileId: string;
  situation?: string;
  timeOfDay?: string;
  moodPreference?: string;
}

export interface PlaylistResponse {
  playlistId: string;
  profileId: string;
  songs: Song[];
  generatedAt: string;
  recommendationReason?: string;
}

// Feedback Types
export interface Feedback {
  id: string;
  profileId: string;
  songId: string;
  rating: number;
  comment?: string;
  timestamp: string;
  profileName?: string;
  songTitle?: string;
}

export interface SubmitFeedbackRequest {
  profileId: string;
  songId: string;
  rating: number;
  comment?: string;
}

export interface FeedbackResponse {
  id: string;
  profileId: string;
  songId: string;
  rating: number;
  comment?: string;
  timestamp: string;
}
