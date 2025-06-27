import apiClient from './api';
import { Movie, PaginatedResponse, MovieFilter } from '../types';

export const movieApi = {
  // Get all movies with pagination and filters
  getMovies: async (
    page: number = 0,
    size: number = 20,
    filters?: MovieFilter
  ): Promise<PaginatedResponse<Movie>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });

    if (filters) {
      if (filters.genre) params.append('genre', filters.genre);
      if (filters.year) params.append('year', filters.year.toString());
      if (filters.minRating) params.append('minRating', filters.minRating.toString());
      if (filters.sortBy) params.append('sortBy', filters.sortBy);
      if (filters.sortDirection) params.append('sortDirection', filters.sortDirection);
    }

    const response = await apiClient.get(`/movies?${params.toString()}`);
    return response.data;
  },

  // Get movie by ID
  getMovieById: async (id: number): Promise<Movie> => {
    const response = await apiClient.get(`/movies/${id}`);
    return response.data;
  },

  // Search movies
  searchMovies: async (query: string, page: number = 0, size: number = 20): Promise<PaginatedResponse<Movie>> => {
    const response = await apiClient.get(`/movies/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}`);
    return response.data;
  },

  // Get popular movies
  getPopularMovies: async (page: number = 0, size: number = 20): Promise<PaginatedResponse<Movie>> => {
    const response = await apiClient.get(`/movies/popular?page=${page}&size=${size}`);
    return response.data;
  },

  // Get movies by genre
  getMoviesByGenre: async (genre: string, page: number = 0, size: number = 20): Promise<PaginatedResponse<Movie>> => {
    const response = await apiClient.get(`/movies/genres/${encodeURIComponent(genre)}?page=${page}&size=${size}`);
    return response.data;
  },

  // Get all available genres
  getGenres: async (): Promise<string[]> => {
    const response = await apiClient.get('/movies/genres');
    return response.data;
  },

  // Get movie recommendations for similar movies
  getSimilarMovies: async (movieId: number): Promise<Movie[]> => {
    const response = await apiClient.get(`/movies/${movieId}/similar`);
    return response.data;
  },
};