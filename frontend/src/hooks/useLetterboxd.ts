import { useState, useCallback } from 'react';
import letterboxdApi from '../services/letterboxdApi';
import { 
  LetterboxdScrapeResponse, 
  LetterboxdProfile, 
  ScrapeOptions 
} from '../types';

interface UseLetterboxdState {
  isLoading: boolean;
  error: string | null;
  serviceHealth: boolean;
  profile: LetterboxdProfile | null;
  scrapeResult: LetterboxdScrapeResponse | null;
}

interface UseLetterboxdActions {
  validateUser: (username: string) => Promise<{ exists: boolean; message: string }>;
  getUserProfile: (username: string) => Promise<LetterboxdProfile | null>;
  scrapeUserData: (username: string, options?: ScrapeOptions) => Promise<LetterboxdScrapeResponse | null>;
  quickScrape: (username: string) => Promise<LetterboxdScrapeResponse | null>;
  checkServiceHealth: () => Promise<boolean>;
  clearError: () => void;
  clearResults: () => void;
}

export const useLetterboxd = (): UseLetterboxdState & UseLetterboxdActions => {
  const [state, setState] = useState<UseLetterboxdState>({
    isLoading: false,
    error: null,
    serviceHealth: false,
    profile: null,
    scrapeResult: null,
  });

  const setLoading = useCallback((loading: boolean) => {
    setState(prev => ({ ...prev, isLoading: loading }));
  }, []);

  const setError = useCallback((error: string | null) => {
    setState(prev => ({ ...prev, error }));
  }, []);

  const validateUser = useCallback(async (username: string) => {
    setLoading(true);
    setError(null);
    
    try {
      const result = await letterboxdApi.validateUser(username);
      
      if (result.exists) {
        // Also fetch profile if user exists
        try {
          const profile = await letterboxdApi.getUserProfile(username);
          setState(prev => ({ ...prev, profile }));
        } catch (profileError) {
          console.warn('Failed to fetch profile:', profileError);
        }
      }
      
      return { exists: result.exists, message: result.message };
    } catch (error: any) {
      const errorMessage = error.response?.data?.error || 'Failed to validate user';
      setError(errorMessage);
      return { exists: false, message: errorMessage };
    } finally {
      setLoading(false);
    }
  }, []);

  const getUserProfile = useCallback(async (username: string) => {
    setLoading(true);
    setError(null);
    
    try {
      const profile = await letterboxdApi.getUserProfile(username);
      setState(prev => ({ ...prev, profile }));
      return profile;
    } catch (error: any) {
      const errorMessage = error.response?.data?.error || 'Failed to fetch user profile';
      setError(errorMessage);
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  const scrapeUserData = useCallback(async (username: string, options?: ScrapeOptions) => {
    setLoading(true);
    setError(null);
    
    try {
      const result = await letterboxdApi.scrapeUserData(username, options);
      setState(prev => ({ ...prev, scrapeResult: result }));
      return result;
    } catch (error: any) {
      const errorMessage = error.response?.data?.error || 'Failed to scrape user data';
      setError(errorMessage);
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  const quickScrape = useCallback(async (username: string) => {
    setLoading(true);
    setError(null);
    
    try {
      const result = await letterboxdApi.quickScrapeUserData(username);
      setState(prev => ({ ...prev, scrapeResult: result }));
      return result;
    } catch (error: any) {
      const errorMessage = error.response?.data?.error || 'Failed to perform quick scrape';
      setError(errorMessage);
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  const checkServiceHealth = useCallback(async () => {
    try {
      const health = await letterboxdApi.healthCheck();
      const isHealthy = health.scraper_available;
      setState(prev => ({ ...prev, serviceHealth: isHealthy }));
      return isHealthy;
    } catch (error) {
      setState(prev => ({ ...prev, serviceHealth: false }));
      return false;
    }
  }, []);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  const clearResults = useCallback(() => {
    setState(prev => ({
      ...prev,
      profile: null,
      scrapeResult: null,
      error: null
    }));
  }, []);

  return {
    ...state,
    validateUser,
    getUserProfile,
    scrapeUserData,
    quickScrape,
    checkServiceHealth,
    clearError,
    clearResults,
  };
};
