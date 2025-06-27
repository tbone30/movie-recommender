import apiClient from './api';
import { Recommendation, PaginatedResponse } from '../types';

export const recommendationApi = {
  // Get user recommendations with pagination
  getRecommendations: async (
    page: number = 0,
    size: number = 20,
    algorithm?: string
  ): Promise<PaginatedResponse<Recommendation>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    
    if (algorithm) {
      params.append('algorithm', algorithm);
    }

    const response = await apiClient.get(`/recommendations?${params.toString()}`);
    return response.data;
  },

  // Refresh recommendations (trigger new recommendation generation)
  refreshRecommendations: async (): Promise<{ message: string }> => {
    const response = await apiClient.post('/recommendations/refresh');
    return response.data;
  },

  // Generate new recommendations
  generateRecommendations: async (algorithm: string = 'hybrid'): Promise<{ message: string }> => {
    const response = await apiClient.post('/recommendations/generate', { algorithm });
    return response.data;
  },

  // Generate cold start recommendations for new users
  generateColdStartRecommendations: async (preferences: {
    genres: string[];
    ratingThreshold: number;
  }): Promise<{ message: string }> => {
    const response = await apiClient.post('/recommendations/cold-start', preferences);
    return response.data;
  },

  // Mark recommendation as viewed
  markViewed: async (recommendationId: number): Promise<{ message: string }> => {
    const response = await apiClient.put(`/recommendations/${recommendationId}/viewed`);
    return response.data;
  },

  // Mark recommendation as clicked
  markClicked: async (recommendationId: number): Promise<{ message: string }> => {
    const response = await apiClient.put(`/recommendations/${recommendationId}/clicked`);
    return response.data;
  },

  // Hide recommendation
  hideRecommendation: async (recommendationId: number): Promise<{ message: string }> => {
    const response = await apiClient.put(`/recommendations/${recommendationId}/hide`);
    return response.data;
  },

  // Get recommendation feedback
  provideFeedback: async (recommendationId: number, feedback: {
    helpful: boolean;
    reason?: string;
  }): Promise<{ message: string }> => {
    const response = await apiClient.post(`/recommendations/${recommendationId}/feedback`, feedback);
    return response.data;
  },
};