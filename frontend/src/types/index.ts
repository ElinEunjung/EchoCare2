// Auth Types
export interface RegisterRequest {
  email: string;
  password: string;
  username: string;
  role: 'CAREGIVER' | 'ADMIN';
}

export interface LoginRequest {
  username: string;
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
  patientName: string;
  era: string;
  favoriteArtists: string[];
  symptoms: string[];
  dementiaStage: 'mild' | 'moderate' | 'severe';
}
export interface CreatePatientProfileRequest {
  patientName: string;
  era: string;
  dementiaStage: 'mild' | 'moderate' | 'severe';
  favoriteArtists: string[];
  symptoms: string[];
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
  patientId: string;
  playlistId: string;
  careNeed: string;
  era: string;
  dementiaStage: string;
  songs: Song[];
  message?: string;
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
  patientId: string;
  careNeed: "stress_relief" | "activity_support" | "calming_agitation" | "easing_depression" | "reducing_anxiety";
  era: string;
  dementiaStage: 'mild' | 'moderate' | 'severe';
  favoriteArtists: string[];
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
  patientProfileId: string;
  playlistId: string;
  liked: boolean;
  timestamp: string;
  profileName?: string;
}

export interface SubmitFeedbackRequest {
  patientProfileId: string;
  playlistId: string;
  liked: boolean;
}

export interface FeedbackResponse {
  id: string;
  patientProfileId: string;
  playlistId: string;
  liked: boolean;
  createdAt: Date;
  
}
