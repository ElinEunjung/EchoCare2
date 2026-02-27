import apiClient from './apiClient';
import type { PatientProfile, CreatePatientProfileRequest, Caregiver } from '../types';

export const profileService = {
  // Patient Profile endpoints
  getAllProfiles: async (): Promise<PatientProfile[]> => {
    const response = await apiClient.get('/api/profiles');
    return response.data;
  },

  getProfileById: async (profileId: string): Promise<PatientProfile> => {
    const response = await apiClient.get(`/api/profiles/${profileId}`);
    return response.data;
  },

  createProfile: async (data: CreatePatientProfileRequest): Promise<PatientProfile> => {

    console.log(data)
    const response = await apiClient.post('/api/profiles', data);


    return response.data;
  },

  updateProfile: async (
    profileId: string,
    data: CreatePatientProfileRequest
  ): Promise<PatientProfile> => {
    const response = await apiClient.put(`/api/profiles/${profileId}`, data);
    return response.data;
  },

  // Caregiver endpoints
  getAllCaregivers: async (): Promise<Caregiver[]> => {
    const response = await apiClient.get('/api/caregivers');
    return response.data;
  },

  getCaregiverById: async (caregiverId: string): Promise<Caregiver> => {
    const response = await apiClient.get(`/api/caregivers/${caregiverId}`);
    return response.data;
  },
};
