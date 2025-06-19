import pandas as pd
import numpy as np
import logging
from datetime import datetime
from typing import Dict, Tuple
import os

logger = logging.getLogger(__name__)

class DatasetBuilder:
    """Builds and manages training datasets from collected user data"""
    
    def __init__(self, db_manager):
        self.db_manager = db_manager
        self.min_ratings_per_user = 10
        self.min_ratings_per_movie = 5
        self.dataset_path = "data/datasets/"
        
        # Ensure dataset directory exists
        os.makedirs(self.dataset_path, exist_ok=True)
        
        logger.info("Dataset builder initialized")
    
    def prepare_training_data(self) -> Dict:
        """Prepare training data from the database"""
        logger.info("Preparing training data")
        
        try:
            # Load ratings data
            ratings_df = self._load_ratings_data()
            
            # Clean and filter data
            clean_ratings = self._clean_data(ratings_df)
            
            # Create user-item matrices
            matrices = self._create_matrices(clean_ratings)
            
            # Split data for training/validation/test
            splits = self._create_train_test_splits(clean_ratings)
            
            # Save processed datasets
            dataset_info = self._save_datasets(matrices, splits)
            
            # Update dataset metrics
            self.update_dataset_metrics()
            
            logger.info("Training data preparation completed")
            return dataset_info
            
        except Exception as e:
            logger.error(f"Error preparing training data: {e}")
            raise
    
    def _load_ratings_data(self) -> pd.DataFrame:
        """Load ratings data from database"""
        query = """
        SELECT 
            r.user_id,
            r.movie_id,
            r.rating,
            r.watched_date,
            r.is_rewatch,
            m.title,
            m.year,
            m.genres
        FROM ratings r
        JOIN movies m ON r.movie_id = m.id
        JOIN users u ON r.user_id = u.id
        WHERE u.is_active = true
        ORDER BY r.user_id, r.watched_date
        """
        
        results = self.db_manager.execute_query(query, fetch=True)
        
        if not results:
            raise ValueError("No ratings data found")
        
        df = pd.DataFrame(results)
        logger.info(f"Loaded {len(df)} ratings from database")
        return df
    
    def _clean_data(self, df: pd.DataFrame) -> pd.DataFrame:
        """Clean and filter the ratings data"""
        logger.info("Cleaning ratings data")
        
        initial_count = len(df)
        
        # Remove duplicates (keep most recent rating for same user-movie pair)
        df = df.sort_values('watched_date').drop_duplicates(
            subset=['user_id', 'movie_id'], 
            keep='last'
        )
        
        # Filter users with minimum ratings
        user_counts = df['user_id'].value_counts()
        valid_users = user_counts[user_counts >= self.min_ratings_per_user].index
        df = df[df['user_id'].isin(valid_users)]
        
        # Filter movies with minimum ratings
        movie_counts = df['movie_id'].value_counts()
        valid_movies = movie_counts[movie_counts >= self.min_ratings_per_movie].index
        df = df[df['movie_id'].isin(valid_movies)]
        
        # Convert rating to float
        df['rating'] = df['rating'].astype(float)
        
        final_count = len(df)
        logger.info(f"Data cleaned: {initial_count} -> {final_count} ratings")
        logger.info(f"Users: {df['user_id'].nunique()}, Movies: {df['movie_id'].nunique()}")
        
        return df
    
    def _create_matrices(self, df: pd.DataFrame) -> Dict:
        """Create user-item interaction matrices"""
        logger.info("Creating user-item matrices")
        
        # Explicit ratings matrix
        ratings_matrix = df.pivot_table(
            index='user_id',
            columns='movie_id',
            values='rating',
            fill_value=0
        )
        
        # Implicit feedback matrix (binary: watched/not watched)
        implicit_matrix = (ratings_matrix > 0).astype(int)
        
        # Calculate sparsity
        total_cells = ratings_matrix.shape[0] * ratings_matrix.shape[1]
        non_zero_cells = (ratings_matrix > 0).sum().sum()
        sparsity = 1 - (non_zero_cells / total_cells)
        
        logger.info(f"Matrix shape: {ratings_matrix.shape}")
        logger.info(f"Sparsity: {sparsity:.4f}")
        
        return {
            'explicit': ratings_matrix,
            'implicit': implicit_matrix,
            'sparsity': sparsity,
            'user_ids': ratings_matrix.index.tolist(),
            'movie_ids': ratings_matrix.columns.tolist()
        }
    
    def _create_train_test_splits(self, df: pd.DataFrame) -> Dict:
        """Create train/validation/test splits"""
        logger.info("Creating train/test splits")
        
        # Sort by timestamp for temporal splitting
        df = df.sort_values('watched_date')
        
        # Split ratios
        train_ratio = 0.7
        val_ratio = 0.15
        test_ratio = 0.15
        
        n = len(df)
        train_size = int(n * train_ratio)
        val_size = int(n * val_ratio)
        
        train_df = df.iloc[:train_size]
        val_df = df.iloc[train_size:train_size + val_size]
        test_df = df.iloc[train_size + val_size:]
        
        logger.info(f"Split sizes - Train: {len(train_df)}, Val: {len(val_df)}, Test: {len(test_df)}")
        
        return {
            'train': train_df,
            'validation': val_df,
            'test': test_df
        }
    
    def _save_datasets(self, matrices: Dict, splits: Dict) -> Dict:
        """Save processed datasets to files"""
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        
        # Save matrices
        matrices['explicit'].to_pickle(f"{self.dataset_path}/ratings_matrix_{timestamp}.pkl")
        matrices['implicit'].to_pickle(f"{self.dataset_path}/implicit_matrix_{timestamp}.pkl")
        
        # Save splits
        for split_name, split_df in splits.items():
            split_df.to_csv(f"{self.dataset_path}/{split_name}_{timestamp}.csv", index=False)
        
        dataset_info = {
            'timestamp': timestamp,
            'explicit_matrix_shape': matrices['explicit'].shape,
            'implicit_matrix_shape': matrices['implicit'].shape,
            'sparsity': matrices['sparsity'],
            'train_size': len(splits['train']),
            'val_size': len(splits['validation']),
            'test_size': len(splits['test']),
            'users': len(matrices['user_ids']),
            'movies': len(matrices['movie_ids'])
        }
        
        logger.info(f"Datasets saved with timestamp: {timestamp}")
        return dataset_info
    
    def get_dataset_statistics(self) -> Dict:
        """Get comprehensive dataset statistics"""
        stats = self.db_manager.get_dataset_stats()
        
        # Add additional computed statistics
        enhanced_stats = {
            **stats,
            'dataset_ready': self.is_ready_for_training(),
            'last_calculated': datetime.now().isoformat()
        }
        
        return enhanced_stats
    
    def update_dataset_metrics(self) -> None:
        """Update dataset metrics in the database"""
        try:
            stats = self.get_dataset_statistics()
            
            # This would update the Spring Boot backend's dataset metrics
            # For now, just log the stats
            logger.info(f"Dataset metrics updated: {stats}")
            
        except Exception as e:
            logger.error(f"Error updating dataset metrics: {e}")
    
    def is_ready_for_training(self) -> bool:
        """Check if dataset meets minimum requirements for training"""
        try:
            stats = self.db_manager.get_dataset_stats()
            
            min_users = 100
            min_ratings = 10000
            
            total_users = stats.get('active_users', 0)
            total_ratings = stats.get('total_ratings', 0)
            
            is_ready = total_users >= min_users and total_ratings >= min_ratings
            
            logger.info(f"Dataset readiness check: {is_ready} (Users: {total_users}/{min_users}, Ratings: {total_ratings}/{min_ratings})")
            
            return is_ready
            
        except Exception as e:
            logger.error(f"Error checking dataset readiness: {e}")
            return False
    
    def export_dataset(self, format_type: str = 'csv') -> str:
        """Export dataset for external analysis"""
        try:
            df = self._load_ratings_data()
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            
            if format_type == 'csv':
                export_path = f"{self.dataset_path}/export_{timestamp}.csv"
                df.to_csv(export_path, index=False)
            elif format_type == 'json':
                export_path = f"{self.dataset_path}/export_{timestamp}.json"
                df.to_json(export_path, orient='records', date_format='iso')
            else:
                raise ValueError(f"Unsupported export format: {format_type}")
            
            logger.info(f"Dataset exported to: {export_path}")
            return export_path
            
        except Exception as e:
            logger.error(f"Error exporting dataset: {e}")
            raise
    
    def get_user_movie_features(self) -> Tuple[pd.DataFrame, pd.DataFrame]:
        """Extract user and movie features for content-based filtering"""
        
        # User features
        user_query = """
        SELECT 
            u.id as user_id,
            COUNT(r.id) as total_ratings,
            AVG(r.rating::float) as avg_rating,
            COUNT(DISTINCT EXTRACT(YEAR FROM r.watched_date)) as active_years,
            array_agg(DISTINCT unnest(m.genres)) as favorite_genres
        FROM users u
        LEFT JOIN ratings r ON u.id = r.user_id
        LEFT JOIN movies m ON r.movie_id = m.id
        WHERE u.is_active = true
        GROUP BY u.id
        """
        
        user_results = self.db_manager.execute_query(user_query, fetch=True)
        user_features = pd.DataFrame(user_results)
        
        # Movie features
        movie_query = """
        SELECT 
            m.id as movie_id,
            m.title,
            m.year,
            m.genres,
            m.director,
            m.runtime,
            m.avg_rating,
            m.rating_count
        FROM movies m
        WHERE m.rating_count >= %s
        """
        
        movie_results = self.db_manager.execute_query(
            movie_query, 
            (self.min_ratings_per_movie,), 
            fetch=True
        )
        movie_features = pd.DataFrame(movie_results)
        
        return user_features, movie_features