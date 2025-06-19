import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { movieApi } from '../services/movieApi';
import { Movie, PaginatedResponse, MovieFilter } from '../types';

interface MovieState {
  movies: Movie[];
  popularMovies: Movie[];
  searchResults: Movie[];
  currentMovie: Movie | null;
  genres: string[];
  isLoading: boolean;
  searchLoading: boolean;
  error: string | null;
  pagination: {
    currentPage: number;
    totalPages: number;
    totalElements: number;
    hasMore: boolean;
  };
  filters: MovieFilter;
}

const initialState: MovieState = {
  movies: [],
  popularMovies: [],
  searchResults: [],
  currentMovie: null,
  genres: [],
  isLoading: false,
  searchLoading: false,
  error: null,
  pagination: {
    currentPage: 0,
    totalPages: 0,
    totalElements: 0,
    hasMore: false,
  },
  filters: {},
};

// Async thunks
export const fetchMovies = createAsyncThunk(
  'movies/fetchMovies',
  async ({ page = 0, size = 20, filters }: { page?: number; size?: number; filters?: MovieFilter }) => {
    const response = await movieApi.getMovies(page, size, filters);
    return response;
  }
);

export const fetchPopularMovies = createAsyncThunk(
  'movies/fetchPopularMovies',
  async () => {
    const response = await movieApi.getPopularMovies();
    return response;
  }
);

export const searchMovies = createAsyncThunk(
  'movies/searchMovies',
  async (query: string) => {
    const response = await movieApi.searchMovies(query);
    return response;
  }
);

export const fetchMovieById = createAsyncThunk(
  'movies/fetchMovieById',
  async (id: number) => {
    const response = await movieApi.getMovieById(id);
    return response;
  }
);

export const fetchGenres = createAsyncThunk(
  'movies/fetchGenres',
  async () => {
    const response = await movieApi.getGenres();
    return response;
  }
);

const movieSlice = createSlice({
  name: 'movies',
  initialState,
  reducers: {
    setFilters: (state, action: PayloadAction<MovieFilter>) => {
      state.filters = { ...state.filters, ...action.payload };
    },
    clearFilters: (state) => {
      state.filters = {};
    },
    clearSearchResults: (state) => {
      state.searchResults = [];
    },
    clearCurrentMovie: (state) => {
      state.currentMovie = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch movies
      .addCase(fetchMovies.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchMovies.fulfilled, (state, action) => {
        state.isLoading = false;
        const response = action.payload as PaginatedResponse<Movie>;
        if (response.number === 0) {
          state.movies = response.content;
        } else {
          state.movies = [...state.movies, ...response.content];
        }
        state.pagination = {
          currentPage: response.number,
          totalPages: response.totalPages,
          totalElements: response.totalElements,
          hasMore: !response.last,
        };
      })
      .addCase(fetchMovies.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Failed to fetch movies';
      })
      // Fetch popular movies
      .addCase(fetchPopularMovies.fulfilled, (state, action) => {
        state.popularMovies = action.payload.content;
      })
      // Search movies
      .addCase(searchMovies.pending, (state) => {
        state.searchLoading = true;
      })
      .addCase(searchMovies.fulfilled, (state, action) => {
        state.searchLoading = false;
        state.searchResults = action.payload.content;
      })
      .addCase(searchMovies.rejected, (state) => {
        state.searchLoading = false;
      })
      // Fetch movie by ID
      .addCase(fetchMovieById.fulfilled, (state, action) => {
        state.currentMovie = action.payload;
      })
      // Fetch genres
      .addCase(fetchGenres.fulfilled, (state, action) => {
        state.genres = action.payload;
      });
  },
});

export const { setFilters, clearFilters, clearSearchResults, clearCurrentMovie } = movieSlice.actions;
export default movieSlice.reducer;