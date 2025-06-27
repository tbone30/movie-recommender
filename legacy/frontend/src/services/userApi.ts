import apiClient from './api';
import { User, Rating, PaginatedResponse, UserStats, SyncStatus } from '../types';

export const userApi = {
  // Get user profile
  getProfile: async (): Promise<User> => {
    const response = await apiClient.get('/users/profile');
    return response.data;
  },

  // Update user profile
  updateProfile: async (updates: Partial<User>): Promise<{ message: string; user: User }> => {
    const response = await apiClient.put('/users/profile', updates);
    return response.data;
  },

  // Sync with Letterboxd
  syncLetterboxd: async (letterboxdUsername: string): Promise<{ message: string }> => {
    const response = await apiClient.post('/users/letterboxd/sync', {
      letterboxd_username: letterboxdUsername,
    });
    return response.data;
  },

  // Get Letterboxd sync status
  getLetterboxdStatus: async (): Promise<{
    letterboxd_username: string;
    sync_status: SyncStatus;
    last_sync_date: string;
  }> => {
    const response = await apiClient.get('/users/letterboxd/status');
    return response.data;
  },

  // Get user ratings
  getRatings: async (page: number = 0, size: number = 20): Promise<PaginatedResponse<Rating>> => {
    const response = await apiClient.get(`/users/ratings?page=${page}&size=${size}`);
    return response.data;
  },

  // Add or update rating
  addRating: async (rating: Omit<Rating, 'id' | 'userId' | 'createdAt' | 'updatedAt'>): Promise<{
    message: string;
    rating: Rating;
  }> => {
    const response = await apiClient.post('/users/ratings', rating);
    return response.data;
  },

  // Update existing rating
  updateRating: async (movieId: number, updates: Partial<Rating>): Promise<{
    message: string;
    rating: Rating;
  }> => {
    const response = await apiClient.put(`/users/ratings/${movieId}`, updates);
    return response.data;
  },

  // Delete rating
  deleteRating: async (movieId: number): Promise<{ message: string }> => {
    const response = await apiClient.delete(`/users/ratings/${movieId}`);
    return response.data;
  },

  // Get user statistics
  getStats: async (): Promise<UserStats> => {
    const response = await apiClient.get('/users/stats');
    return response.data;
  },

  // Delete user account
  deleteAccount: async (): Promise<{ message: string }> => {
    const response = await apiClient.delete('/users/account');
    return response.data;
  },
};