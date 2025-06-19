// Core entity types matching the backend
export interface User {
  id: number;
  username: string;
  email: string;
  roles: UserRole[];
  letterboxdUsername?: string;
  syncStatus?: SyncStatus;
  lastSyncDate?: string;
  createdAt: string;
  updatedAt: string;
}

export enum UserRole {
  USER = 'USER',
  ADMIN = 'ADMIN'
}

export enum SyncStatus {
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED'
}

export interface Movie {
  id: number;
  title: string;
  originalTitle?: string;
  overview?: string;
  genres: string[];
  releaseYear: number;
  director?: string;
  tmdbId?: number;
  imdbId?: string;
  letterboxdId?: string;
  posterUrl?: string;
  backdropUrl?: string;
  runtime?: number;
  averageRating: number;
  ratingCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface Rating {
  id: number;
  userId: number;
  movieId: number;
  rating: number;
  review?: string;
  isWatched: boolean;
  watchedDate?: string;
  createdAt: string;
  updatedAt: string;
  movie?: Movie;
}

export interface Recommendation {
  id: number;
  userId: number;
  movieId: number;
  score: number;
  algorithm: string;
  reason?: string;
  isViewed: boolean;
  isClicked: boolean;
  isHidden: boolean;
  createdAt: string;
  movie?: Movie;
}

export interface DatasetMetrics {
  id: number;
  totalUsers: number;
  totalMovies: number;
  totalRatings: number;
  avgRatingsPerUser: number;
  avgRatingsPerMovie: number;
  sparsity: number;
  trainingStatus: TrainingStatus;
  lastTrainingDate?: string;
  modelAccuracy?: number;
  createdAt: string;
  updatedAt: string;
}

export enum TrainingStatus {
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED'
}

// API response types
export interface ApiResponse<T> {
  data?: T;
  message?: string;
  error?: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  id: number;
  username: string;
  email: string;
  roles: UserRole[];
}

// Form types
export interface LoginRequest {
  username: string;
  password: string;
}

export interface SignupRequest {
  username: string;
  email: string;
  password: string;
}

export interface MovieFilter {
  genre?: string;
  year?: number;
  minRating?: number;
  sortBy?: 'title' | 'releaseYear' | 'averageRating' | 'ratingCount';
  sortDirection?: 'asc' | 'desc';
}

export interface UserStats {
  totalRatings: number;
  averageRating: number;
  favoriteGenres: string[];
  recentActivity: Rating[];
  recommendationCount: number;
}