import apiClient from './apiClient';
import type { SubmitFeedbackRequest, FeedbackResponse } from '../types';

export const feedbackService = {
  submitFeedback: async (data: SubmitFeedbackRequest): Promise<FeedbackResponse> => {
    const response = await apiClient.post('/api/feedback', data);
    return response.data;
  },

  getFeedbackForProfile: async (profileId: string): Promise<FeedbackResponse[]> => {
    const response = await apiClient.get(`/api/feedback/profile/${profileId}`);
    return response.data;
  },
};
