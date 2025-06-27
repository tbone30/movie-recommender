import React, { useEffect } from 'react';
import {
  Container,
  Typography,
  Box,
  Button,
  Grid,
  Card,
  CardContent,
  CardMedia,
  CardActions,
  Chip,
  CircularProgress,
} from '@mui/material';
import { Link, useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store';
import { fetchPopularMovies } from '../store/movieSlice';
import { Movie } from '../types';

const Home: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch<AppDispatch>();
  const { isAuthenticated } = useSelector((state: RootState) => state.auth);
  const { popularMovies, isLoading } = useSelector((state: RootState) => state.movies);

  useEffect(() => {
    dispatch(fetchPopularMovies());
  }, [dispatch]);

  const handleMovieClick = (movieId: number) => {
    navigate(`/movies/${movieId}`);
  };

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Box textAlign="center" mb={6}>
        <Typography variant="h2" component="h1" gutterBottom>
          Welcome to Movie Recommender
        </Typography>
        <Typography variant="h5" color="text.secondary" paragraph>
          Discover your next favorite movie with personalized recommendations
          powered by Letterboxd data and machine learning
        </Typography>
        
        {!isAuthenticated && (
          <Box mt={4}>
            <Button
              variant="contained"
              size="large"
              component={Link}
              to="/register"
              sx={{ mr: 2 }}
            >
              Get Started
            </Button>
            <Button
              variant="outlined"
              size="large"
              component={Link}
              to="/login"
            >
              Sign In
            </Button>
          </Box>
        )}
      </Box>

      <Box mb={6}>
        <Typography variant="h4" component="h2" gutterBottom>
          Popular Movies
        </Typography>
        
        {isLoading ? (
          <Box display="flex" justifyContent="center" py={4}>
            <CircularProgress />
          </Box>
        ) : (
          <Grid container spacing={3}>
            {popularMovies.slice(0, 8).map((movie: Movie) => (
              <Grid item xs={12} sm={6} md={3} key={movie.id}>
                <Card 
                  sx={{ 
                    height: '100%',
                    display: 'flex',
                    flexDirection: 'column',
                    cursor: 'pointer',
                    '&:hover': {
                      transform: 'translateY(-4px)',
                      transition: 'transform 0.2s ease-in-out',
                    },
                  }}
                  onClick={() => handleMovieClick(movie.id)}
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
                      {movie.releaseYear}
                    </Typography>
                    <Box mb={1}>
                      {movie.genres.slice(0, 2).map((genre) => (
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
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        )}
      </Box>

      <Box textAlign="center" py={6}>
        <Typography variant="h4" component="h2" gutterBottom>
          How It Works
        </Typography>
        <Grid container spacing={4} mt={2}>
          <Grid item xs={12} md={4}>
            <Box>
              <Typography variant="h6" gutterBottom>
                1. Connect Letterboxd
              </Typography>
              <Typography color="text.secondary">
                Link your Letterboxd account to import your movie ratings and preferences
              </Typography>
            </Box>
          </Grid>
          <Grid item xs={12} md={4}>
            <Box>
              <Typography variant="h6" gutterBottom>
                2. AI Analysis
              </Typography>
              <Typography color="text.secondary">
                Our machine learning algorithms analyze your taste and find patterns
              </Typography>
            </Box>
          </Grid>
          <Grid item xs={12} md={4}>
            <Box>
              <Typography variant="h6" gutterBottom>
                3. Get Recommendations
              </Typography>
              <Typography color="text.secondary">
                Receive personalized movie recommendations tailored to your preferences
              </Typography>
            </Box>
          </Grid>
        </Grid>
      </Box>
    </Container>
  );
};

export default Home;