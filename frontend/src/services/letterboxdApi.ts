import api from './api';
import { 
  LetterboxdProfile, 
  LetterboxdScrapeResponse, 
  ScrapeOptions,
  LetterboxdRating,
  LetterboxdWatchlistFilm
} from '../types';

export interface LetterboxdServiceStatus {
  enabled: boolean;
  baseUrl: string;
  timeout: number;
  healthy: boolean;
}

export interface UserValidationResponse {
  username: string;
  exists: boolean;
  message: string;
}

/**
 * Transform raw JSON response to properly typed LetterboxdRating objects
 */
const transformRatings = (rawRatings: any[]): LetterboxdRating[] => {
  if (!Array.isArray(rawRatings)) return [];
  
  return rawRatings.map(rating => ({
    filmTitle: rating.filmTitle || rating.film_title || '',
    filmYear: rating.filmYear || rating.film_year || undefined,
    filmSlug: rating.filmSlug || rating.film_slug || undefined,
    rating: typeof rating.rating === 'number' ? rating.rating : undefined,
    watchedDate: rating.watchedDate || rating.watched_date || undefined,
    review: rating.review || undefined,
    letterboxdUri: rating.letterboxdUri || rating.letterboxd_uri || undefined,
  })).filter(rating => rating.filmTitle); // Filter out entries without titles
};

/**
 * Transform raw JSON response to properly typed LetterboxdWatchlistFilm objects
 */
const transformWatchlist = (rawWatchlist: any[]): LetterboxdWatchlistFilm[] => {
  if (!Array.isArray(rawWatchlist)) return [];
  
  return rawWatchlist.map(film => ({
    filmTitle: film.filmTitle || film.film_title || '',
    filmYear: film.filmYear || film.film_year || undefined,
    filmSlug: film.filmSlug || film.film_slug || undefined,
    directors: Array.isArray(film.directors) ? film.directors : 
               Array.isArray(film.director) ? film.director : [],
    genres: Array.isArray(film.genres) ? film.genres : 
            Array.isArray(film.genre) ? film.genre : [],
    addedDate: film.addedDate || film.added_date || undefined,
    letterboxdUri: film.letterboxdUri || film.letterboxd_uri || undefined,
  })).filter(film => film.filmTitle); // Filter out entries without titles
};

/**
 * Transform raw JSON response to properly typed LetterboxdProfile object
 */
const transformProfile = (rawProfile: any): LetterboxdProfile => {
  if (!rawProfile || typeof rawProfile !== 'object') {
    throw new Error('Invalid profile data received');
  }
  
  return {
    username: rawProfile.username || '',
    displayName: rawProfile.displayName || rawProfile.display_name || undefined,
    bio: rawProfile.bio || undefined,
    location: rawProfile.location || undefined,
    website: rawProfile.website || undefined,
    joinedDate: rawProfile.joinedDate || rawProfile.joined_date || undefined,
    filmsWatched: typeof rawProfile.filmsWatched === 'number' ? rawProfile.filmsWatched :
                  typeof rawProfile.films_watched === 'number' ? rawProfile.films_watched : undefined,
    followers: typeof rawProfile.followers === 'number' ? rawProfile.followers : undefined,
    following: typeof rawProfile.following === 'number' ? rawProfile.following : undefined,
  };
};

/**
 * Transform raw JSON scrape response to properly typed LetterboxdScrapeResponse
 */
const transformScrapeResponse = (rawResponse: any): LetterboxdScrapeResponse => {
  if (!rawResponse || typeof rawResponse !== 'object') {
    throw new Error('Invalid scrape response data received');
  }
  
  return {
    username: rawResponse.username || '',
    profile: rawResponse.profile ? transformProfile(rawResponse.profile) : undefined,
    ratings: rawResponse.ratings ? transformRatings(rawResponse.ratings) : [],
    watchlist: rawResponse.watchlist ? transformWatchlist(rawResponse.watchlist) : [],
    scrapedAt: rawResponse.scrapedAt || rawResponse.scraped_at || undefined,
    totalRatings: typeof rawResponse.totalRatings === 'number' ? rawResponse.totalRatings :
                  typeof rawResponse.total_ratings === 'number' ? rawResponse.total_ratings : 0,
    totalWatchlistItems: typeof rawResponse.totalWatchlistItems === 'number' ? rawResponse.totalWatchlistItems :
                         typeof rawResponse.total_watchlist_items === 'number' ? rawResponse.total_watchlist_items : 0,
    success: Boolean(rawResponse.success),
    errorMessage: rawResponse.errorMessage || rawResponse.error_message || undefined,
  };
};

class LetterboxdApiService {
  private readonly baseUrl = 'letterboxd';

  /**
   * Get the status of the Letterboxd scraper service
   */
  async getServiceStatus(): Promise<LetterboxdServiceStatus> {
    const response = await api.get(`${this.baseUrl}/status`);
    return response.data;
  }

  /**
   * Validate if a Letterboxd user exists
   */
  async validateUser(username: string): Promise<UserValidationResponse> {
    const response = await api.get(`${this.baseUrl}/user/${username}/validate`);
    return response.data;
  }

  /**
   * Get user profile information only
   */
  async getUserProfile(username: string): Promise<LetterboxdProfile> {
    try {
      const response = await api.get(`${this.baseUrl}/user/${username}/profile`);
      return transformProfile(response.data);
    } catch (error: any) {
      if (error.response?.status === 404) {
        throw new Error(`User '${username}' not found`);
      }
      throw new Error(`Failed to fetch profile: ${error.message}`);
    }
  }

  /**
   * Scrape complete user data with custom options
   */
  async scrapeUserData(
    username: string, 
    options: ScrapeOptions = {}
  ): Promise<LetterboxdScrapeResponse> {
    try {
      const {
        includeRatings = true,
        includeWatchlist = true,
        ratingLimit = 100
      } = options;

      const params = new URLSearchParams({
        includeRatings: includeRatings.toString(),
        includeWatchlist: includeWatchlist.toString(),
        ratingLimit: ratingLimit.toString()
      });

      const response = await api.post(`${this.baseUrl}/user/${username}/scrape?${params}`);
      return transformScrapeResponse(response.data);
    } catch (error: any) {
      if (error.response?.status === 404) {
        throw new Error(`User '${username}' not found`);
      }
      throw new Error(`Failed to scrape user data: ${error.message}`);
    }
  }

  /**
   * Quick scrape with default settings (50 rating limit)
   */
  async quickScrapeUserData(username: string): Promise<LetterboxdScrapeResponse> {
    try {
      const response = await api.post(`${this.baseUrl}/user/${username}/scrape/quick`);
      return transformScrapeResponse(response.data);
    } catch (error: any) {
      if (error.response?.status === 404) {
        throw new Error(`User '${username}' not found`);
      }
      throw new Error(`Failed to perform quick scrape: ${error.message}`);
    }
  }

  /**
   * Check if the scraper service is healthy
   */
  async healthCheck(): Promise<{ status: string; service: string; scraper_available: boolean }> {
    const response = await api.get(`${this.baseUrl}/health`);
    return response.data;
  }
}

export const letterboxdApi = new LetterboxdApiService();
export default letterboxdApi;
