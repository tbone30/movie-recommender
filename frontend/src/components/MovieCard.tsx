import React from 'react';
import {
  Card,
  CardContent,
  CardMedia,
  Typography,
  Box,
  Chip,
  IconButton,
  Tooltip,
} from '@mui/material';
import { Favorite, FavoriteBorder, Star } from '@mui/icons-material';
import { Movie } from '../types';

interface MovieCardProps {
  movie: Movie;
  onClick?: () => void;
  showActions?: boolean;
  matchScore?: number;
  algorithm?: string;
}

const MovieCard: React.FC<MovieCardProps> = ({
  movie,
  onClick,
  showActions = false,
  matchScore,
  algorithm,
}) => {
  const handleCardClick = (e: React.MouseEvent) => {
    if (onClick) {
      onClick();
    }
  };

  return (
    <Card
      sx={{
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        cursor: onClick ? 'pointer' : 'default',
        '&:hover': onClick ? {
          transform: 'translateY(-2px)',
          transition: 'transform 0.2s ease-in-out',
          boxShadow: 3,
        } : {},
      }}
      onClick={handleCardClick}
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
      <CardContent sx={{ flexGrow: 1, p: 2 }}>
        <Typography variant="h6" component="h3" gutterBottom noWrap>
          {movie.title}
        </Typography>
        
        <Typography variant="body2" color="text.secondary" gutterBottom>
          {movie.releaseYear} {movie.director && `• ${movie.director}`}
        </Typography>

        {matchScore && algorithm && (
          <Box mb={1}>
            <Chip
              label={`${algorithm.replace('-', ' ')}`}
              color="primary"
              size="small"
              sx={{ mr: 1 }}
            />
            <Chip
              label={`${Math.round(matchScore * 100)}% match`}
              color="info"
              size="small"
            />
          </Box>
        )}

        <Box mb={1}>
          {movie.genres.slice(0, 3).map((genre) => (
            <Chip
              key={genre}
              label={genre}
              size="small"
              variant="outlined"
              sx={{ mr: 0.5, mb: 0.5 }}
            />
          ))}
        </Box>

        <Box display="flex" alignItems="center" mb={1}>
          <Star sx={{ color: 'gold', fontSize: 16, mr: 0.5 }} />
          <Typography variant="body2" color="text.secondary">
            {movie.averageRating.toFixed(1)} ({movie.ratingCount} ratings)
          </Typography>
        </Box>

        {movie.overview && (
          <Typography
            variant="body2"
            color="text.secondary"
            sx={{
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

        {showActions && (
          <Box display="flex" justifyContent="space-between" alignItems="center" mt={2}>
            <Tooltip title="Add to favorites">
              <IconButton size="small">
                <FavoriteBorder />
              </IconButton>
            </Tooltip>
          </Box>
        )}
      </CardContent>
    </Card>
  );
};

export default MovieCard;