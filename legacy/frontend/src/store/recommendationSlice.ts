import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { recommendationApi } from '../services/recommendationApi';
import { Recommendation, PaginatedResponse } from '../types';

interface RecommendationState {
  recommendations: Recommendation[];
  isLoading: boolean;
  error: string | null;
  pagination: {
    currentPage: number;
    totalPages: number;
    totalElements: number;
    hasMore: boolean;
  };
  refreshing: boolean;
}

const initialState: RecommendationState = {
  recommendations: [],
  isLoading: false,
  error: null,
  pagination: {
    currentPage: 0,
    totalPages: 0,
    totalElements: 0,
    hasMore: false,
  },
  refreshing: false,
};

// Async thunks
export const fetchRecommendations = createAsyncThunk(
  'recommendations/fetchRecommendations',
  async ({ page = 0, size = 20 }: { page?: number; size?: number } = {}) => {
    const response = await recommendationApi.getRecommendations(page, size);
    return response;
  }
);

export const refreshRecommendations = createAsyncThunk(
  'recommendations/refreshRecommendations',
  async () => {
    const response = await recommendationApi.refreshRecommendations();
    return response;
  }
);

export const markRecommendationViewed = createAsyncThunk(
  'recommendations/markViewed',
  async (recommendationId: number) => {
    await recommendationApi.markViewed(recommendationId);
    return recommendationId;
  }
);

export const markRecommendationClicked = createAsyncThunk(
  'recommendations/markClicked',
  async (recommendationId: number) => {
    await recommendationApi.markClicked(recommendationId);
    return recommendationId;
  }
);

export const hideRecommendation = createAsyncThunk(
  'recommendations/hideRecommendation',
  async (recommendationId: number) => {
    await recommendationApi.hideRecommendation(recommendationId);
    return recommendationId;
  }
);

const recommendationSlice = createSlice({
  name: 'recommendations',
  initialState,
  reducers: {
    clearRecommendations: (state) => {
      state.recommendations = [];
      state.pagination = {
        currentPage: 0,
        totalPages: 0,
        totalElements: 0,
        hasMore: false,
      };
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch recommendations
      .addCase(fetchRecommendations.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchRecommendations.fulfilled, (state, action) => {
        state.isLoading = false;
        const response = action.payload as PaginatedResponse<Recommendation>;
        if (response.number === 0) {
          state.recommendations = response.content;
        } else {
          state.recommendations = [...state.recommendations, ...response.content];
        }
        state.pagination = {
          currentPage: response.number,
          totalPages: response.totalPages,
          totalElements: response.totalElements,
          hasMore: !response.last,
        };
      })
      .addCase(fetchRecommendations.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Failed to fetch recommendations';
      })
      // Refresh recommendations
      .addCase(refreshRecommendations.pending, (state) => {
        state.refreshing = true;
      })
      .addCase(refreshRecommendations.fulfilled, (state) => {
        state.refreshing = false;
        // Clear existing recommendations to force refetch
        state.recommendations = [];
        state.pagination = {
          currentPage: 0,
          totalPages: 0,
          totalElements: 0,
          hasMore: false,
        };
      })
      .addCase(refreshRecommendations.rejected, (state) => {
        state.refreshing = false;
      })
      // Mark viewed
      .addCase(markRecommendationViewed.fulfilled, (state, action) => {
        const recommendationId = action.payload;
        const recommendation = state.recommendations.find(r => r.id === recommendationId);
        if (recommendation) {
          recommendation.isViewed = true;
        }
      })
      // Mark clicked
      .addCase(markRecommendationClicked.fulfilled, (state, action) => {
        const recommendationId = action.payload;
        const recommendation = state.recommendations.find(r => r.id === recommendationId);
        if (recommendation) {
          recommendation.isClicked = true;
        }
      })
      // Hide recommendation
      .addCase(hideRecommendation.fulfilled, (state, action) => {
        const recommendationId = action.payload;
        state.recommendations = state.recommendations.filter(r => r.id !== recommendationId);
      });
  },
});

export const { clearRecommendations } = recommendationSlice.actions;
export default recommendationSlice.reducer;