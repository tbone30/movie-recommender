export interface Movie {
  id?: number;
  title: string;
  genre?: string;
  director?: string;
  releaseYear?: number;
  rating?: number;
  description?: string;
}

export interface User {
  id?: number;
  username: string;
  email: string;
  letterboxdUsername?: string;
}

export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}

// Letterboxd related types
export interface LetterboxdProfile {
  username: string;
  displayName?: string;
  bio?: string;
  profileImageUrl?: string;
  totalFilms?: number;
  totalRatings?: number;
  totalWatchlistItems?: number;
}

export interface LetterboxdMovie {
  title: string;
  year?: number;
  director?: string;
  rating?: number;
  letterboxdUrl?: string;
  tmdbId?: string;
  imdbId?: string;
}

export interface LetterboxdScrapeResponse {
  success: boolean;
  errorMessage?: string;
  profile?: LetterboxdProfile;
  ratings?: LetterboxdMovie[];
  watchlist?: LetterboxdMovie[];
  totalRatings: number;
  totalWatchlistItems: number;
  processingTime?: number;
}

export interface ScrapeOptions {
  includeRatings?: boolean;
  includeWatchlist?: boolean;
  ratingLimit?: number;
}
