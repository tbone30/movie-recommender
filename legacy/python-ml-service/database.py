import os
import psycopg2
import psycopg2.extras
import redis
import logging
from urllib.parse import urlparse

logger = logging.getLogger(__name__)

class DatabaseManager:
    """Manages database connections and operations"""
    
    def __init__(self):
        # Parse database URL from environment
        database_url = os.environ.get('DATABASE_URL', 'postgresql://localhost:5432/movierecommender')
        
        if database_url.startswith('postgres://'):
            database_url = database_url.replace('postgres://', 'postgresql://', 1)
        
        parsed_url = urlparse(database_url)
        
        self.db_config = {
            'host': parsed_url.hostname,
            'port': parsed_url.port or 5432,
            'database': parsed_url.path[1:],  # Remove leading '/'
            'user': parsed_url.username,
            'password': parsed_url.password
        }
        
        # Redis configuration
        redis_url = os.environ.get('REDIS_URL', 'redis://localhost:6379')
        self.redis_client = redis.from_url(redis_url)
        
        logger.info("Database manager initialized")
    
    def get_connection(self):
        """Get a database connection"""
        try:
            conn = psycopg2.connect(**self.db_config)
            return conn
        except Exception as e:
            logger.error(f"Database connection error: {e}")
            raise
    
    def execute_query(self, query, params=None, fetch=False):
        """Execute a database query"""
        conn = None
        try:
            conn = self.get_connection()
            with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cursor:
                cursor.execute(query, params)
                
                if fetch:
                    if cursor.description:
                        return cursor.fetchall()
                    return []
                else:
                    conn.commit()
                    return cursor.rowcount
                    
        except Exception as e:
            if conn:
                conn.rollback()
            logger.error(f"Query execution error: {e}")
            raise
        finally:
            if conn:
                conn.close()
    
    def execute_many(self, query, params_list):
        """Execute a query with multiple parameter sets"""
        conn = None
        try:
            conn = self.get_connection()
            with conn.cursor() as cursor:
                cursor.executemany(query, params_list)
                conn.commit()
                return cursor.rowcount
        except Exception as e:
            if conn:
                conn.rollback()
            logger.error(f"Batch execution error: {e}")
            raise
        finally:
            if conn:
                conn.close()
    
    def get_user_by_letterboxd_username(self, letterboxd_username):
        """Find user by Letterboxd username"""
        query = """
        SELECT id, username, letterboxd_username, sync_status, last_sync_date
        FROM users 
        WHERE letterboxd_username = %s AND is_active = true
        """
        results = self.execute_query(query, (letterboxd_username,), fetch=True)
        return results[0] if results else None
    
    def find_or_create_movie(self, title, year, tmdb_id=None, letterboxd_id=None):
        """Find existing movie or create new one"""
        # Try to find existing movie
        find_query = """
        SELECT id FROM movies 
        WHERE title = %s AND year = %s
        """
        results = self.execute_query(find_query, (title, year), fetch=True)
        
        if results:
            return results[0]['id']
        
        # Create new movie
        insert_query = """
        INSERT INTO movies (title, year, tmdb_id, letterboxd_id, created_at)
        VALUES (%s, %s, %s, %s, NOW())
        RETURNING id
        """
        results = self.execute_query(
            insert_query, 
            (title, year, tmdb_id, letterboxd_id), 
            fetch=True
        )
        return results[0]['id']
    
    def save_rating(self, user_id, movie_id, rating, review_text, watched_date, is_rewatch=False):
        """Save a user rating"""
        query = """
        INSERT INTO ratings (user_id, movie_id, rating, review_text, watched_date, is_rewatch, created_at)
        VALUES (%s, %s, %s, %s, %s, %s, NOW())
        ON CONFLICT (user_id, movie_id, watched_date) 
        DO UPDATE SET 
            rating = EXCLUDED.rating,
            review_text = EXCLUDED.review_text,
            is_rewatch = EXCLUDED.is_rewatch,
            updated_at = NOW()
        """
        return self.execute_query(
            query, 
            (user_id, movie_id, rating, review_text, watched_date, is_rewatch)
        )
    
    def get_user_ratings(self, user_id, limit=None):
        """Get all ratings for a user"""
        query = """
        SELECT r.*, m.title, m.year 
        FROM ratings r
        JOIN movies m ON r.movie_id = m.id
        WHERE r.user_id = %s
        ORDER BY r.watched_date DESC
        """
        if limit:
            query += f" LIMIT {limit}"
            
        return self.execute_query(query, (user_id,), fetch=True)
    
    def get_dataset_stats(self):
        """Get dataset statistics"""
        stats_query = """
        SELECT 
            COUNT(DISTINCT u.id) as total_users,
            COUNT(DISTINCT m.id) as total_movies,
            COUNT(r.id) as total_ratings,
            AVG(r.rating::float) as avg_rating,
            COUNT(DISTINCT r.user_id) as active_users,
            COUNT(DISTINCT CASE WHEN m.rating_count >= 5 THEN m.id END) as popular_movies
        FROM users u
        CROSS JOIN movies m
        LEFT JOIN ratings r ON u.id = r.user_id
        WHERE u.is_active = true
        """
        results = self.execute_query(stats_query, fetch=True)
        return results[0] if results else {}
    
    def cache_set(self, key, value, expiration=3600):
        """Set value in Redis cache"""
        try:
            self.redis_client.setex(key, expiration, str(value))
        except Exception as e:
            logger.warning(f"Cache set error: {e}")
    
    def cache_get(self, key):
        """Get value from Redis cache"""
        try:
            value = self.redis_client.get(key)
            return value.decode('utf-8') if value else None
        except Exception as e:
            logger.warning(f"Cache get error: {e}")
            return None
    
    def cache_exists(self, key):
        """Check if key exists in cache"""
        try:
            return self.redis_client.exists(key)
        except Exception as e:
            logger.warning(f"Cache exists error: {e}")
            return False