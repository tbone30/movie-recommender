import pandas as pd
import numpy as np
import logging
from datetime import datetime
from typing import Dict, List, Tuple, Optional
from model_trainer import ModelTrainer

logger = logging.getLogger(__name__)

class RecommendationEngine:
    """Generates movie recommendations using trained ML models"""
    
    def __init__(self, db_manager):
        self.db_manager = db_manager
        self.model_trainer = ModelTrainer(db_manager)
        self.models = None
        self.current_algorithm = "hybrid"
        self.default_num_recommendations = 20
        
        # Load models on initialization
        self._load_models()
        
        logger.info("Recommendation engine initialized")
    
    def _load_models(self):
        """Load the latest trained models"""
        try:
            self.models = self.model_trainer.load_latest_models()
            logger.info(f"Loaded models version: {self.models.get('version', 'unknown')}")
        except Exception as e:
            logger.warning(f"Could not load models: {e}")
            self.models = None
    
    def get_recommendations_for_user(self, user_id: int, num_recommendations: int = None) -> List[Dict]:
        """Generate personalized recommendations for a user"""
        if num_recommendations is None:
            num_recommendations = self.default_num_recommendations
        
        logger.info(f"Generating {num_recommendations} recommendations for user {user_id}")
        
        if not self.models:
            logger.warning("No models available, returning fallback recommendations")
            return self._get_fallback_recommendations(user_id, num_recommendations)
        
        try:
            # Get user's rating history
            user_ratings = self._get_user_ratings(user_id)
            
            if len(user_ratings) < 5:
                # Cold start - use content-based or popular movies
                return self.get_cold_start_recommendations(user_id, num_recommendations=num_recommendations)
            
            # Generate recommendations based on current algorithm
            if self.current_algorithm == "hybrid":
                recommendations = self._generate_hybrid_recommendations(user_id, user_ratings, num_recommendations)
            elif self.current_algorithm == "collaborative":
                recommendations = self._generate_collaborative_recommendations(user_id, user_ratings, num_recommendations)
            elif self.current_algorithm == "content":
                recommendations = self._generate_content_recommendations(user_id, user_ratings, num_recommendations)
            else:
                recommendations = self._generate_hybrid_recommendations(user_id, user_ratings, num_recommendations)
            
            # Add explanations and format output
            formatted_recommendations = self._format_recommendations(recommendations, user_ratings)
            
            logger.info(f"Generated {len(formatted_recommendations)} recommendations for user {user_id}")
            return formatted_recommendations
            
        except Exception as e:
            logger.error(f"Error generating recommendations for user {user_id}: {e}")
            return self._get_fallback_recommendations(user_id, num_recommendations)
    
    def get_cold_start_recommendations(self, user_id: int, preferred_genres: List[str] = None, 
                                     num_recommendations: int = None) -> List[Dict]:
        """Generate recommendations for new users with limited data"""
        if num_recommendations is None:
            num_recommendations = self.default_num_recommendations
        
        logger.info(f"Generating cold start recommendations for user {user_id}")
        
        try:
            # Get popular movies
            popular_movies = self._get_popular_movies(num_recommendations * 2)
            
            # Filter by preferred genres if provided
            if preferred_genres:
                filtered_movies = []
                for movie in popular_movies:
                    movie_genres = movie.get('genres', [])
                    if any(genre in movie_genres for genre in preferred_genres):
                        filtered_movies.append(movie)
                
                if len(filtered_movies) >= num_recommendations:
                    popular_movies = filtered_movies[:num_recommendations]
                else:
                    # Mix filtered and popular if not enough genre matches
                    remaining = num_recommendations - len(filtered_movies)
                    for movie in popular_movies:
                        if movie not in filtered_movies and remaining > 0:
                            filtered_movies.append(movie)
                            remaining -= 1
                    popular_movies = filtered_movies
            
            # Format as recommendations
            recommendations = []
            for i, movie in enumerate(popular_movies[:num_recommendations]):
                recommendations.append({
                    'movie_id': movie['id'],
                    'title': movie['title'],
                    'year': movie['year'],
                    'score': 0.8 - (i * 0.02),  # Decreasing score based on popularity rank
                    'explanation': f"Popular movie in your preferred genres" if preferred_genres else "Highly rated popular movie",
                    'rank': i + 1
                })
            
            return recommendations
            
        except Exception as e:
            logger.error(f"Error generating cold start recommendations: {e}")
            return self._get_fallback_recommendations(user_id, num_recommendations)
    
    def _generate_hybrid_recommendations(self, user_id: int, user_ratings: pd.DataFrame, 
                                       num_recommendations: int) -> List[Dict]:
        """Generate recommendations using hybrid approach"""
        try:
            # Get collaborative filtering recommendations
            cf_recs = self._generate_collaborative_recommendations(user_id, user_ratings, num_recommendations * 2)
            
            # Get content-based recommendations
            cb_recs = self._generate_content_recommendations(user_id, user_ratings, num_recommendations * 2)
            
            # Combine and re-rank
            combined_scores = {}
            
            # Weight collaborative filtering more heavily
            cf_weight = 0.7
            cb_weight = 0.3
            
            for rec in cf_recs:
                movie_id = rec['movie_id']
                combined_scores[movie_id] = cf_weight * rec['score']
            
            for rec in cb_recs:
                movie_id = rec['movie_id']
                if movie_id in combined_scores:
                    combined_scores[movie_id] += cb_weight * rec['score']
                else:
                    combined_scores[movie_id] = cb_weight * rec['score']
            
            # Sort by combined score
            sorted_recommendations = sorted(combined_scores.items(), key=lambda x: x[1], reverse=True)
            
            # Format recommendations
            recommendations = []
            for i, (movie_id, score) in enumerate(sorted_recommendations[:num_recommendations]):
                movie_info = self._get_movie_info(movie_id)
                recommendations.append({
                    'movie_id': movie_id,
                    'title': movie_info.get('title', 'Unknown'),
                    'year': movie_info.get('year', 0),
                    'score': float(score),
                    'explanation': 'Recommended based on your viewing history and similar movies',
                    'rank': i + 1
                })
            
            return recommendations
            
        except Exception as e:
            logger.error(f"Error in hybrid recommendations: {e}")
            return []
    
    def _generate_collaborative_recommendations(self, user_id: int, user_ratings: pd.DataFrame, 
                                              num_recommendations: int) -> List[Dict]:
        """Generate recommendations using collaborative filtering"""
        try:
            # Use user-based collaborative filtering
            user_similarity = self.models['user_similarity']
            ratings_matrix = self.models.get('ratings_matrix')
            
            if ratings_matrix is None:
                raise ValueError("Ratings matrix not available")
            
            # Find user index in the matrix
            if user_id not in ratings_matrix.index:
                # User not in training data, use content-based approach
                return self._generate_content_recommendations(user_id, user_ratings, num_recommendations)
            
            user_idx = list(ratings_matrix.index).index(user_id)
            user_similarities = user_similarity[user_idx]
            
            # Find similar users
            similar_users = np.argsort(user_similarities)[::-1][1:11]  # Top 10 similar users, excluding self
            
            # Get recommendations from similar users
            recommendations = {}
            
            for similar_user_idx in similar_users:
                similar_user_id = ratings_matrix.index[similar_user_idx]
                similar_user_ratings = ratings_matrix.iloc[similar_user_idx]
                similarity_score = user_similarities[similar_user_idx]
                
                # Get highly rated movies by similar user that current user hasn't seen
                user_watched_movies = set(user_ratings['movie_id'].values)
                
                for movie_id, rating in similar_user_ratings.items():
                    if rating > 3.5 and movie_id not in user_watched_movies:
                        if movie_id not in recommendations:
                            recommendations[movie_id] = 0
                        recommendations[movie_id] += rating * similarity_score
            
            # Sort and format recommendations
            sorted_recs = sorted(recommendations.items(), key=lambda x: x[1], reverse=True)
            
            formatted_recs = []
            for i, (movie_id, score) in enumerate(sorted_recs[:num_recommendations]):
                movie_info = self._get_movie_info(movie_id)
                formatted_recs.append({
                    'movie_id': movie_id,
                    'title': movie_info.get('title', 'Unknown'),
                    'year': movie_info.get('year', 0),
                    'score': min(float(score) / 5.0, 1.0),  # Normalize to 0-1
                    'explanation': 'Recommended based on users with similar taste',
                    'rank': i + 1
                })
            
            return formatted_recs
            
        except Exception as e:
            logger.error(f"Error in collaborative filtering: {e}")
            return []
    
    def _generate_content_recommendations(self, user_id: int, user_ratings: pd.DataFrame, 
                                        num_recommendations: int) -> List[Dict]:
        """Generate recommendations using content-based filtering"""
        try:
            content_similarity = self.models['content_similarity']
            
            # Get user's highly rated movies
            high_rated_movies = user_ratings[user_ratings['rating'] >= 4.0]['movie_id'].values
            
            if len(high_rated_movies) == 0:
                # No highly rated movies, use all movies with positive ratings
                high_rated_movies = user_ratings[user_ratings['rating'] >= 3.0]['movie_id'].values
            
            # Find movies similar to user's highly rated movies
            recommendations = {}
            user_watched_movies = set(user_ratings['movie_id'].values)
            
            # Get all movies info for mapping
            all_movies = self._get_all_movies()
            movie_id_to_idx = {movie['id']: i for i, movie in enumerate(all_movies)}
            
            for movie_id in high_rated_movies:
                if movie_id in movie_id_to_idx:
                    movie_idx = movie_id_to_idx[movie_id]
                    
                    # Get similar movies
                    similarities = content_similarity[movie_idx]
                    similar_movie_indices = np.argsort(similarities)[::-1][1:21]  # Top 20, excluding self
                    
                    for similar_idx in similar_movie_indices:
                        similar_movie = all_movies[similar_idx]
                        similar_movie_id = similar_movie['id']
                        
                        if similar_movie_id not in user_watched_movies:
                            similarity_score = similarities[similar_idx]
                            
                            if similar_movie_id not in recommendations:
                                recommendations[similar_movie_id] = 0
                            recommendations[similar_movie_id] += similarity_score
            
            # Sort and format recommendations
            sorted_recs = sorted(recommendations.items(), key=lambda x: x[1], reverse=True)
            
            formatted_recs = []
            for i, (movie_id, score) in enumerate(sorted_recs[:num_recommendations]):
                movie_info = self._get_movie_info(movie_id)
                formatted_recs.append({
                    'movie_id': movie_id,
                    'title': movie_info.get('title', 'Unknown'),
                    'year': movie_info.get('year', 0),
                    'score': min(float(score), 1.0),
                    'explanation': 'Recommended based on movies you\'ve enjoyed',
                    'rank': i + 1
                })
            
            return formatted_recs
            
        except Exception as e:
            logger.error(f"Error in content-based filtering: {e}")
            return []
    
    def _get_user_ratings(self, user_id: int) -> pd.DataFrame:
        """Get user's rating history"""
        ratings = self.db_manager.get_user_ratings(user_id)
        return pd.DataFrame(ratings)
    
    def _get_movie_info(self, movie_id: int) -> Dict:
        """Get movie information"""
        query = """
        SELECT id, title, year, genres, director, avg_rating, rating_count
        FROM movies 
        WHERE id = %s
        """
        results = self.db_manager.execute_query(query, (movie_id,), fetch=True)
        return results[0] if results else {}
    
    def _get_all_movies(self) -> List[Dict]:
        """Get all movies for content similarity mapping"""
        query = """
        SELECT id, title, year, genres, director, avg_rating, rating_count
        FROM movies 
        ORDER BY id
        """
        results = self.db_manager.execute_query(query, fetch=True)
        return results
    
    def _get_popular_movies(self, limit: int = 50) -> List[Dict]:
        """Get popular movies for cold start recommendations"""
        query = """
        SELECT id, title, year, genres, avg_rating, rating_count
        FROM movies 
        WHERE rating_count >= 10
        ORDER BY avg_rating DESC, rating_count DESC
        LIMIT %s
        """
        results = self.db_manager.execute_query(query, (limit,), fetch=True)
        return results
    
    def _get_fallback_recommendations(self, user_id: int, num_recommendations: int) -> List[Dict]:
        """Fallback recommendations when models are not available"""
        logger.info(f"Using fallback recommendations for user {user_id}")
        
        popular_movies = self._get_popular_movies(num_recommendations)
        
        recommendations = []
        for i, movie in enumerate(popular_movies):
            recommendations.append({
                'movie_id': movie['id'],
                'title': movie['title'],
                'year': movie['year'],
                'score': 0.8 - (i * 0.01),
                'explanation': 'Popular highly-rated movie',
                'rank': i + 1
            })
        
        return recommendations
    
    def _format_recommendations(self, recommendations: List[Dict], user_ratings: pd.DataFrame) -> List[Dict]:
        """Format and enrich recommendations with additional info"""
        formatted = []
        
        for rec in recommendations:
            movie_info = self._get_movie_info(rec['movie_id'])
            
            formatted_rec = {
                **rec,
                'title': movie_info.get('title', rec.get('title', 'Unknown')),
                'year': movie_info.get('year', rec.get('year', 0)),
                'genres': movie_info.get('genres', []),
                'director': movie_info.get('director'),
                'avg_rating': float(movie_info.get('avg_rating', 0)) if movie_info.get('avg_rating') else None,
                'rating_count': movie_info.get('rating_count', 0)
            }
            
            formatted.append(formatted_rec)
        
        return formatted
    
    def get_current_algorithm(self) -> str:
        """Get current recommendation algorithm"""
        return self.current_algorithm
    
    def set_algorithm(self, algorithm: str):
        """Set recommendation algorithm"""
        if algorithm in ['hybrid', 'collaborative', 'content']:
            self.current_algorithm = algorithm
            logger.info(f"Algorithm changed to: {algorithm}")
        else:
            raise ValueError(f"Unknown algorithm: {algorithm}")
    
    def get_model_version(self) -> str:
        """Get current model version"""
        return self.models.get('version', '1.0.0') if self.models else '1.0.0'
    
    def refresh_models(self):
        """Reload models from disk"""
        logger.info("Refreshing models")
        self._load_models()