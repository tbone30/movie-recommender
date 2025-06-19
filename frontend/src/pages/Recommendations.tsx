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
  Button,
  CircularProgress,
  Alert,
  IconButton,
  Tooltip,
  Pagination,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import { 
  Refresh as RefreshIcon, 
  ThumbUp, 
  ThumbDown, 
  Visibility,
  VisibilityOff 
} from '@mui/icons-material';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store';
import { 
  fetchRecommendations, 
  refreshRecommendations, 
  markRecommendationViewed,
  markRecommendationClicked,
  hideRecommendation 
} from '../store/recommendationSlice';
import { Recommendation } from '../types';

const Recommendations: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const { 
    recommendations, 
    isLoading, 
    refreshing, 
    error, 
    pagination 
  } = useSelector((state: RootState) => state.recommendations);

  const [algorithmFilter, setAlgorithmFilter] = useState('');
  const algorithms = ['hybrid', 'collaborative', 'content-based', 'matrix-factorization'];

  useEffect(() => {
    dispatch(fetchRecommendations());
  }, [dispatch]);

  const handleRefresh = () => {
    dispatch(refreshRecommendations()).then(() => {
      dispatch(fetchRecommendations());
    });
  };

  const handleMovieClick = (recommendation: Recommendation) => {
    dispatch(markRecommendationClicked(recommendation.id));
    dispatch(markRecommendationViewed(recommendation.id));
    // Navigate to movie details or open movie modal
  };

  const handleHideRecommendation = (recommendationId: number) => {
    dispatch(hideRecommendation(recommendationId));
  };

  const handlePageChange = (event: React.ChangeEvent<unknown>, value: number) => {
    dispatch(fetchRecommendations({ page: value - 1 }));
  };

  const handleAlgorithmFilter = (algorithm: string) => {
    setAlgorithmFilter(algorithm);
    dispatch(fetchRecommendations({ page: 0, size: 20 }));
  };

  const getAlgorithmColor = (algorithm: string) => {
    switch (algorithm) {
      case 'hybrid': return 'primary';
      case 'collaborative': return 'secondary';
      case 'content-based': return 'success';
      case 'matrix-factorization': return 'warning';
      default: return 'default';
    }
  };

  const getReasonText = (recommendation: Recommendation) => {
    if (recommendation.reason) return recommendation.reason;
    
    switch (recommendation.algorithm) {
      case 'collaborative':
        return 'Recommended based on users with similar taste';
      case 'content-based':
        return 'Recommended based on movies you liked';
      case 'hybrid':
        return 'Recommended using multiple algorithms';
      case 'matrix-factorization':
        return 'Discovered through pattern analysis';
      default:
        return 'Personalized recommendation';
    }
  };

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={4}>
        <Typography variant="h3" component="h1">
          Your Recommendations
        </Typography>
        <Button
          variant="contained"
          startIcon={<RefreshIcon />}
          onClick={handleRefresh}
          disabled={refreshing}
        >
          {refreshing ? <CircularProgress size={20} /> : 'Refresh'}
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 4 }}>
          {error}
        </Alert>
      )}

      {/* Algorithm Filter */}
      <Box sx={{ mb: 4 }}>
        <FormControl sx={{ minWidth: 200 }}>
          <InputLabel>Filter by Algorithm</InputLabel>
          <Select
            value={algorithmFilter}
            label="Filter by Algorithm"
            onChange={(e) => handleAlgorithmFilter(e.target.value)}
          >
            <MenuItem value="">All Algorithms</MenuItem>
            {algorithms.map((algorithm) => (
              <MenuItem key={algorithm} value={algorithm}>
                {algorithm.charAt(0).toUpperCase() + algorithm.slice(1).replace('-', ' ')}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>

      {isLoading ? (
        <Box display="flex" justifyContent="center" py={4}>
          <CircularProgress />
        </Box>
      ) : recommendations.length === 0 ? (
        <Box textAlign="center" py={8}>
          <Typography variant="h5" color="text.secondary" gutterBottom>
            No recommendations available
          </Typography>
          <Typography variant="body1" color="text.secondary" paragraph>
            Connect your Letterboxd account to get personalized recommendations
          </Typography>
          <Button variant="contained" href="/profile">
            Go to Profile
          </Button>
        </Box>
      ) : (
        <>
          <Grid container spacing={3}>
            {recommendations.map((recommendation: Recommendation) => (
              <Grid item xs={12} sm={6} md={4} key={recommendation.id}>
                <Card 
                  sx={{ 
                    height: '100%',
                    display: 'flex',
                    flexDirection: 'column',
                    cursor: 'pointer',
                    opacity: recommendation.isViewed ? 0.8 : 1,
                    '&:hover': {
                      transform: 'translateY(-2px)',
                      transition: 'transform 0.2s ease-in-out',
                    },
                  }}
                  onClick={() => handleMovieClick(recommendation)}
                >
                  {recommendation.movie?.posterUrl && (
                    <CardMedia
                      component="img"
                      height="300"
                      image={recommendation.movie.posterUrl}
                      alt={recommendation.movie.title}
                      sx={{ objectFit: 'cover' }}
                    />
                  )}
                  <CardContent sx={{ flexGrow: 1 }}>
                    <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={1}>
                      <Typography variant="h6" component="h3" sx={{ flexGrow: 1 }}>
                        {recommendation.movie?.title}
                      </Typography>
                      <Tooltip title="Hide recommendation">
                        <IconButton 
                          size="small" 
                          onClick={(e) => {
                            e.stopPropagation();
                            handleHideRecommendation(recommendation.id);
                          }}
                        >
                          <VisibilityOff fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    </Box>
                    
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      {recommendation.movie?.releaseYear} • {recommendation.movie?.director}
                    </Typography>

                    <Box mb={2}>
                      <Chip
                        label={`${recommendation.algorithm.replace('-', ' ')}`}
                        color={getAlgorithmColor(recommendation.algorithm) as any}
                        size="small"
                        sx={{ mr: 1 }}
                      />
                      <Chip
                        label={`${(recommendation.score * 100).toFixed(0)}% match`}
                        color="info"
                        size="small"
                      />
                    </Box>

                    <Box mb={1}>
                      {recommendation.movie?.genres.slice(0, 3).map((genre) => (
                        <Chip
                          key={genre}
                          label={genre}
                          size="small"
                          variant="outlined"
                          sx={{ mr: 0.5, mb: 0.5 }}
                        />
                      ))}
                    </Box>

                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      ⭐ {recommendation.movie?.averageRating.toFixed(1)} 
                      ({recommendation.movie?.ratingCount} ratings)
                    </Typography>

                    <Typography 
                      variant="body2" 
                      color="primary"
                      sx={{ 
                        fontStyle: 'italic',
                        mt: 1,
                        fontSize: '0.85rem'
                      }}
                    >
                      {getReasonText(recommendation)}
                    </Typography>

                    {recommendation.movie?.overview && (
                      <Typography 
                        variant="body2" 
                        color="text.secondary" 
                        sx={{ 
                          mt: 1,
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          display: '-webkit-box',
                          WebkitLineClamp: 2,
                          WebkitBoxOrient: 'vertical',
                        }}
                      >
                        {recommendation.movie.overview}
                      </Typography>
                    )}

                    <Box display="flex" alignItems="center" justifyContent="space-between" mt={2}>
                      <Box>
                        {recommendation.isViewed && (
                          <Chip 
                            icon={<Visibility />} 
                            label="Viewed" 
                            size="small" 
                            variant="outlined" 
                          />
                        )}
                        {recommendation.isClicked && (
                          <Chip 
                            label="Clicked" 
                            size="small" 
                            color="success"
                            variant="outlined"
                            sx={{ ml: 0.5 }}
                          />
                        )}
                      </Box>
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>

          {/* Pagination */}
          {pagination.totalPages > 1 && (
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

      {/* Recommendation Statistics */}
      {recommendations.length > 0 && (
        <Box mt={6} p={3} bgcolor="background.paper" borderRadius={2}>
          <Typography variant="h6" gutterBottom>
            Recommendation Statistics
          </Typography>
          <Grid container spacing={2}>
            <Grid item xs={6} sm={3}>
              <Typography variant="body2" color="text.secondary">
                Total Recommendations
              </Typography>
              <Typography variant="h6">
                {pagination.totalElements}
              </Typography>
            </Grid>
            <Grid item xs={6} sm={3}>
              <Typography variant="body2" color="text.secondary">
                Average Match Score
              </Typography>
              <Typography variant="h6">
                {recommendations.length > 0 
                  ? Math.round(recommendations.reduce((sum, r) => sum + r.score, 0) / recommendations.length * 100)
                  : 0}%
              </Typography>
            </Grid>
            <Grid item xs={6} sm={3}>
              <Typography variant="body2" color="text.secondary">
                Viewed
              </Typography>
              <Typography variant="h6">
                {recommendations.filter(r => r.isViewed).length}
              </Typography>
            </Grid>
            <Grid item xs={6} sm={3}>
              <Typography variant="body2" color="text.secondary">
                Clicked
              </Typography>
              <Typography variant="h6">
                {recommendations.filter(r => r.isClicked).length}
              </Typography>
            </Grid>
          </Grid>
        </Box>
      )}
    </Container>
  );
};

export default Recommendations;