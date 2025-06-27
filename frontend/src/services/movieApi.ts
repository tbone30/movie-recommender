import api from './api';
import { Movie } from '../types';

export const movieApi = {
  // Get all movies
  getAllMovies: async (): Promise<Movie[]> => {
    const response = await api.get('/movies/getAll');
    return response.data;
  },

  // Get movie by ID
  getMovieById: async (id: number): Promise<Movie> => {
    const response = await api.get(`/movies/${id}`);
    return response.data;
  },

  // Create new movie
  createMovie: async (movie: Omit<Movie, 'id'>): Promise<Movie> => {
    const response = await api.post('/movies', movie);
    return response.data;
  },

  // Update movie
  updateMovie: async (id: number, movie: Partial<Movie>): Promise<Movie> => {
    const response = await api.put(`/movies/${id}`, movie);
    return response.data;
  },

  // Delete movie
  deleteMovie: async (id: number): Promise<void> => {
    await api.delete(`/movies/${id}`);
  },

  // Search movies
  searchMovies: async (query: string): Promise<Movie[]> => {
    const response = await api.get(`/movies/search?q=${encodeURIComponent(query)}`);
    return response.data;
  },
};
