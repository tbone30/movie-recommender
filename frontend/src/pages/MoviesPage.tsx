import React, { useState, useEffect } from 'react';
import { movieApi } from '../services/movieApi';
import { Movie } from '../types';
import MovieCard from '../components/MovieCard';
import MovieForm from '../components/MovieForm';

const MoviesPage: React.FC = () => {
  const [movies, setMovies] = useState<Movie[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [editingMovie, setEditingMovie] = useState<Movie | undefined>(undefined);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    loadMovies();
  }, []);

  const loadMovies = async () => {
    try {
      setLoading(true);
      setError(null);
      const moviesData = await movieApi.getAllMovies();
      setMovies(moviesData);
    } catch (err) {
      setError('Failed to load movies. Please check if the backend is running.');
      console.error('Error loading movies:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddMovie = () => {
    setEditingMovie(undefined);
    setShowForm(true);
  };

  const handleEditMovie = (movie: Movie) => {
    setEditingMovie(movie);
    setShowForm(true);
  };

  const handleDeleteMovie = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this movie?')) {
      try {
        await movieApi.deleteMovie(id);
        await loadMovies();
      } catch (err) {
        setError('Failed to delete movie');
        console.error('Error deleting movie:', err);
      }
    }
  };

  const handleFormSubmit = async (movieData: Omit<Movie, 'id'>) => {
    try {
      if (editingMovie) {
        await movieApi.updateMovie(editingMovie.id!, movieData);
      } else {
        await movieApi.createMovie(movieData);
      }
      setShowForm(false);
      setEditingMovie(undefined);
      await loadMovies();
    } catch (err) {
      setError('Failed to save movie');
      console.error('Error saving movie:', err);
    }
  };

  const handleFormCancel = () => {
    setShowForm(false);
    setEditingMovie(undefined);
  };

  const handleSearch = async () => {
    if (searchQuery.trim()) {
      try {
        setLoading(true);
        const searchResults = await movieApi.searchMovies(searchQuery);
        setMovies(searchResults);
      } catch (err) {
        setError('Failed to search movies');
        console.error('Error searching movies:', err);
      } finally {
        setLoading(false);
      }
    } else {
      loadMovies();
    }
  };

  if (loading) {
    return <div className="loading">Loading movies...</div>;
  }

  return (
    <div className="movies-page">
      <div className="movies-header">
        <h1>Movies</h1>
        <div className="movies-actions">
          <div className="search-bar">
            <input
              type="text"
              placeholder="Search movies..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
            />
            <button onClick={handleSearch} className="btn btn-search">
              Search
            </button>
            {searchQuery && (
              <button onClick={() => { setSearchQuery(''); loadMovies(); }} className="btn btn-clear">
                Clear
              </button>
            )}
          </div>
          <button onClick={handleAddMovie} className="btn btn-primary">
            Add Movie
          </button>
        </div>
      </div>

      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      <div className="movies-grid">
        {movies.length === 0 ? (
          <div className="no-movies">
            {searchQuery ? 'No movies found matching your search.' : 'No movies found. Add some movies to get started!'}
          </div>
        ) : (
          movies.map(movie => (
            <MovieCard
              key={movie.id}
              movie={movie}
              onEdit={handleEditMovie}
              onDelete={handleDeleteMovie}
            />
          ))
        )}
      </div>

      {showForm && (
        <MovieForm
          movie={editingMovie}
          onSubmit={handleFormSubmit}
          onCancel={handleFormCancel}
        />
      )}
    </div>
  );
};

export default MoviesPage;
