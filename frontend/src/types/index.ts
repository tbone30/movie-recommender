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
  location?: string;
  website?: string;
  joinedDate?: string;
  filmsWatched?: number;
  followers?: number;
  following?: number;
}

export interface LetterboxdRating {
  filmTitle: string;
  filmYear?: number;
  filmSlug?: string;
  rating?: number;
  watchedDate?: string;
  review?: string;
  letterboxdUri?: string;
}

export interface LetterboxdWatchlistFilm {
  filmTitle: string;
  filmYear?: number;
  filmSlug?: string;
  directors?: string[];
  genres?: string[];
  addedDate?: string;
  letterboxdUri?: string;
}

export interface LetterboxdScrapeResponse {
  username: string;
  profile?: LetterboxdProfile;
  ratings?: LetterboxdRating[];
  watchlist?: LetterboxdWatchlistFilm[];
  scrapedAt?: string;
  totalRatings: number;
  totalWatchlistItems: number;
  success: boolean;
  errorMessage?: string;
}

export interface ScrapeOptions {
  includeRatings?: boolean;
  includeWatchlist?: boolean;
  ratingLimit?: number;
}
