import React, { useEffect, useState } from 'react';
import {
  Container,
  Typography,
  Box,
  Grid,
  Card,
  CardContent,
  CardMedia,
  Chip,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Button,
  CircularProgress,
  Pagination,
} from '@mui/material';
import { Search as SearchIcon } from '@mui/icons-material';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store';
import { fetchMovies, searchMovies, setFilters, clearSearchResults, fetchGenres } from '../store/movieSlice';
import { Movie } from '../types';

const Movies: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const { 
    movies, 
    searchResults, 
    genres, 
    isLoading, 
    searchLoading, 
    pagination, 
    filters 
  } = useSelector((state: RootState) => state.movies);

  const [searchQuery, setSearchQuery] = useState('');
  const [localFilters, setLocalFilters] = useState({
    genre: '',
    year: '',
    minRating: '',
    sortBy: 'title',
    sortDirection: 'asc',
  });

  useEffect(() => {
    dispatch(fetchGenres());
    dispatch(fetchMovies({ page: 0 }));
  }, [dispatch]);

  const handleSearch = () => {
    if (searchQuery.trim()) {
      dispatch(searchMovies(searchQuery));
    } else {
      dispatch(clearSearchResults());
    }
  };

  const handleFilterChange = (field: string, value: string) => {
    const newFilters = { ...localFilters, [field]: value };
    setLocalFilters(newFilters);
    
    const filterData = {
      genre: newFilters.genre || undefined,
      year: newFilters.year ? parseInt(newFilters.year) : undefined,
      minRating: newFilters.minRating ? parseFloat(newFilters.minRating) : undefined,
      sortBy: newFilters.sortBy as any,
      sortDirection: newFilters.sortDirection as any,
    };
    
    dispatch(setFilters(filterData));
    dispatch(fetchMovies({ page: 0, filters: filterData }));
  };

  const handlePageChange = (event: React.ChangeEvent<unknown>, value: number) => {
    dispatch(fetchMovies({ page: value - 1, filters }));
  };

  const clearSearch = () => {
    setSearchQuery('');
    dispatch(clearSearchResults());
  };

  const displayMovies = searchResults.length > 0 ? searchResults : movies;

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h3" component="h1" gutterBottom>
        Movies
      </Typography>

      {/* Search Bar */}
      <Box sx={{ mb: 4, display: 'flex', gap: 2, alignItems: 'center' }}>
        <TextField
          fullWidth
          label="Search movies..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
        />
        <Button
          variant="contained"
          startIcon={<SearchIcon />}
          onClick={handleSearch}
          disabled={searchLoading}
        >
          {searchLoading ? <CircularProgress size={20} /> : 'Search'}
        </Button>
        {searchResults.length > 0 && (
          <Button variant="outlined" onClick={clearSearch}>
            Clear
          </Button>
        )}
      </Box>

      {/* Filters */}
      <Box sx={{ mb: 4, display: 'flex', gap: 2, flexWrap: 'wrap' }}>
        <FormControl sx={{ minWidth: 120 }}>
          <InputLabel>Genre</InputLabel>
          <Select
            value={localFilters.genre}
            label="Genre"
            onChange={(e) => handleFilterChange('genre', e.target.value)}
          >
            <MenuItem value="">All Genres</MenuItem>
            {genres.map((genre) => (
              <MenuItem key={genre} value={genre}>
                {genre}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <TextField
          label="Year"
          type="number"
          value={localFilters.year}
          onChange={(e) => handleFilterChange('year', e.target.value)}
          sx={{ width: 100 }}
        />

        <TextField
          label="Min Rating"
          type="number"
          inputProps={{ min: 0, max: 10, step: 0.1 }}
          value={localFilters.minRating}
          onChange={(e) => handleFilterChange('minRating', e.target.value)}
          sx={{ width: 120 }}
        />

        <FormControl sx={{ minWidth: 120 }}>
          <InputLabel>Sort By</InputLabel>
          <Select
            value={localFilters.sortBy}
            label="Sort By"
            onChange={(e) => handleFilterChange('sortBy', e.target.value)}
          >
            <MenuItem value="title">Title</MenuItem>
            <MenuItem value="releaseYear">Year</MenuItem>
            <MenuItem value="averageRating">Rating</MenuItem>
            <MenuItem value="ratingCount">Popularity</MenuItem>
          </Select>
        </FormControl>

        <FormControl sx={{ minWidth: 120 }}>
          <InputLabel>Order</InputLabel>
          <Select
            value={localFilters.sortDirection}
            label="Order"
            onChange={(e) => handleFilterChange('sortDirection', e.target.value)}
          >
            <MenuItem value="asc">Ascending</MenuItem>
            <MenuItem value="desc">Descending</MenuItem>
          </Select>
        </FormControl>
      </Box>

      {/* Results */}
      {searchResults.length > 0 && (
        <Typography variant="h6" sx={{ mb: 2 }}>
          Search Results ({searchResults.length} movies)
        </Typography>
      )}

      {isLoading ? (
        <Box display="flex" justifyContent="center" py={4}>
          <CircularProgress />
        </Box>
      ) : (
        <>
          <Grid container spacing={3}>
            {displayMovies.map((movie: Movie) => (
              <Grid item xs={12} sm={6} md={4} lg={3} key={movie.id}>
                <Card 
                  sx={{ 
                    height: '100%',
                    display: 'flex',
                    flexDirection: 'column',
                    cursor: 'pointer',
                    '&:hover': {
                      transform: 'translateY(-2px)',
                      transition: 'transform 0.2s ease-in-out',
                    },
                  }}
                >
                  {movie.posterUrl && (
                    <CardMedia
                      component="img"
                      height="300"
                      image={movie.posterUrl}
                      alt={movie.title}
                      sx={{ objectFit: 'cover' }}
                    />
                  )}
                  <CardContent sx={{ flexGrow: 1 }}>
                    <Typography variant="h6" component="h3" gutterBottom>
                      {movie.title}
                    </Typography>
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      {movie.releaseYear} • {movie.director}
                    </Typography>
                    <Box mb={1}>
                      {movie.genres.slice(0, 3).map((genre) => (
                        <Chip
                          key={genre}
                          label={genre}
                          size="small"
                          sx={{ mr: 0.5, mb: 0.5 }}
                        />
                      ))}
                    </Box>
                    <Typography variant="body2" color="text.secondary">
                      ⭐ {movie.averageRating.toFixed(1)} ({movie.ratingCount} ratings)
                    </Typography>
                    {movie.overview && (
                      <Typography 
                        variant="body2" 
                        color="text.secondary" 
                        sx={{ 
                          mt: 1,
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          display: '-webkit-box',
                          WebkitLineClamp: 3,
                          WebkitBoxOrient: 'vertical',
                        }}
                      >
                        {movie.overview}
                      </Typography>
                    )}
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>

          {/* Pagination */}
          {searchResults.length === 0 && pagination.totalPages > 1 && (
            <Box display="flex" justifyContent="center" mt={4}>
              <Pagination
                count={pagination.totalPages}
                page={pagination.currentPage + 1}
                onChange={handlePageChange}
                color="primary"
              />
            </Box>
          )}
        </>
      )}
    </Container>
  );
};

export default Movies;