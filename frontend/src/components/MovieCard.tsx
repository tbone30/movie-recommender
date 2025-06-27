import React from 'react';
import { Movie } from '../types';

interface MovieCardProps {
  movie: Movie;
  onEdit?: (movie: Movie) => void;
  onDelete?: (id: number) => void;
}

const MovieCard: React.FC<MovieCardProps> = ({ movie, onEdit, onDelete }) => {
  return (
    <div className="movie-card">
      <div className="movie-card-header">
        <h3 className="movie-title">{movie.title}</h3>
        {movie.rating && (
          <span className="movie-rating">⭐ {movie.rating.toFixed(1)}</span>
        )}
      </div>
      
      <div className="movie-details">
        {movie.genre && <p><strong>Genre:</strong> {movie.genre}</p>}
        {movie.director && <p><strong>Director:</strong> {movie.director}</p>}
        {movie.releaseYear && <p><strong>Year:</strong> {movie.releaseYear}</p>}
        {movie.description && <p><strong>Description:</strong> {movie.description}</p>}
      </div>
      
      <div className="movie-actions">
        {onEdit && (
          <button 
            onClick={() => onEdit(movie)}
            className="btn btn-edit"
          >
            Edit
          </button>
        )}
        {onDelete && movie.id && (
          <button 
            onClick={() => onDelete(movie.id!)}
            className="btn btn-delete"
          >
            Delete
          </button>
        )}
      </div>
    </div>
  );
};

export default MovieCard;
