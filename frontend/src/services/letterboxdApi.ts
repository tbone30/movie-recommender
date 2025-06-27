import api from './api';

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
  error_message?: string;
  profile?: LetterboxdProfile;
  ratings?: LetterboxdMovie[];
  watchlist?: LetterboxdMovie[];
  total_ratings: number;
  total_watchlist_items: number;
  processing_time?: number;
}

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

export interface ScrapeOptions {
  includeRatings?: boolean;
  includeWatchlist?: boolean;
  ratingLimit?: number;
}

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
    const response = await api.get(`${this.baseUrl}/user/${username}/profile`);
    return response.data;
  }

  /**
   * Scrape complete user data with custom options
   */
  async scrapeUserData(
    username: string, 
    options: ScrapeOptions = {}
  ): Promise<LetterboxdScrapeResponse> {
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
    return response.data;
  }

  /**
   * Quick scrape with default settings (50 rating limit)
   */
  async quickScrapeUserData(username: string): Promise<LetterboxdScrapeResponse> {
    const response = await api.post(`${this.baseUrl}/user/${username}/scrape/quick`);
    return response.data;
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
