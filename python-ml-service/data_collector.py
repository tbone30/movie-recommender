import time
import random
import logging
import requests
from datetime import datetime, timedelta
from typing import Dict, List, Optional
import re

logger = logging.getLogger(__name__)

class LetterboxdDataCollector:
    """Collects data from Letterboxd user profiles using web scraping"""
    
    def __init__(self, db_manager):
        self.db_manager = db_manager
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
        })
        
        # Rate limiting
        self.min_delay = 1.0  # Minimum delay between requests
        self.max_delay = 3.0  # Maximum delay between requests
        self.max_retries = 3
        self.backoff_factor = 2
        
        logger.info("Letterboxd data collector initialized")
    
    def collect_user_data(self, letterboxd_username: str) -> Dict:
        """Collect comprehensive data for a single Letterboxd user"""
        logger.info(f"Starting data collection for {letterboxd_username}")
        
        # Check cache first
        cache_key = f"letterboxd_user_{letterboxd_username}"
        if self.db_manager.cache_exists(cache_key):
            logger.info(f"Using cached data for {letterboxd_username}")
            return {"status": "cached", "username": letterboxd_username}
        
        try:
            # Get user from database
            user = self.db_manager.get_user_by_letterboxd_username(letterboxd_username)
            if not user:
                logger.warning(f"User {letterboxd_username} not found in database")
                return {"status": "user_not_found", "username": letterboxd_username}
            
            user_id = user['id']
            
            # Collect user profile data
            profile_data = self._scrape_user_profile(letterboxd_username)
            
            # Collect watched films with ratings
            films_data = self._scrape_user_films(letterboxd_username)
            
            # Process and save the data
            processed_count = self._process_and_save_films(user_id, films_data)
            
            # Cache the result
            self.db_manager.cache_set(cache_key, "completed", expiration=3600)  # 1 hour cache
            
            result = {
                "status": "success",
                "username": letterboxd_username,
                "user_id": user_id,
                "profile_data": profile_data,
                "films_processed": processed_count,
                "total_films": len(films_data)
            }
            
            logger.info(f"Data collection completed for {letterboxd_username}: {processed_count} films processed")
            return result
            
        except Exception as e:
            logger.error(f"Error collecting data for {letterboxd_username}: {e}")
            raise
    
    def bulk_collect_users(self, usernames: List[str]) -> Dict:
        """Collect data for multiple users with rate limiting"""
        results = {
            "successful": [],
            "failed": [],
            "cached": []
        }
        
        for i, username in enumerate(usernames):
            try:
                logger.info(f"Processing user {i+1}/{len(usernames)}: {username}")
                
                result = self.collect_user_data(username)
                
                if result["status"] == "success":
                    results["successful"].append(result)
                elif result["status"] == "cached":
                    results["cached"].append(result)
                else:
                    results["failed"].append(result)
                
                # Rate limiting between users
                if i < len(usernames) - 1:  # Don't delay after the last user
                    delay = random.uniform(self.min_delay, self.max_delay)
                    time.sleep(delay)
                    
            except Exception as e:
                logger.error(f"Failed to collect data for {username}: {e}")
                results["failed"].append({
                    "username": username,
                    "error": str(e)
                })
        
        return results
    
    def _scrape_user_profile(self, username: str) -> Dict:
        """Scrape user profile information"""
        url = f"https://letterboxd.com/{username}/"
        
        try:
            response = self._make_request(url)
            
            # Simple parsing - in production you'd use BeautifulSoup
            profile_data = {
                "username": username,
                "profile_url": url,
                "scraped_at": datetime.now().isoformat()
            }
            
            # Extract basic stats from the page
            content = response.text
            
            # Look for film count (this is a simplified regex - you'd want more robust parsing)
            films_match = re.search(r'(\d+)\s+films?', content, re.IGNORECASE)
            if films_match:
                profile_data["total_films"] = int(films_match.group(1))
            
            # Look for followers count
            followers_match = re.search(r'(\d+)\s+followers?', content, re.IGNORECASE)
            if followers_match:
                profile_data["followers"] = int(followers_match.group(1))
            
            return profile_data
            
        except Exception as e:
            logger.error(f"Error scraping profile for {username}: {e}")
            return {"username": username, "error": str(e)}
    
    def _scrape_user_films(self, username: str, pages_limit: int = 10) -> List[Dict]:
        """Scrape user's watched films with ratings"""
        films = []
        
        for page in range(1, pages_limit + 1):
            try:
                url = f"https://letterboxd.com/{username}/films/page/{page}/"
                response = self._make_request(url)
                
                # Parse films from this page
                page_films = self._parse_films_page(response.text, username)
                
                if not page_films:
                    # No more films, break
                    break
                
                films.extend(page_films)
                
                # Rate limiting between pages
                time.sleep(random.uniform(0.5, 1.5))
                
            except Exception as e:
                logger.error(f"Error scraping films page {page} for {username}: {e}")
                break
        
        return films
    
    def _parse_films_page(self, html_content: str, username: str) -> List[Dict]:
        """Parse films from a Letterboxd films page"""
        films = []
        
        # This is a simplified implementation
        # In production, you'd use BeautifulSoup or similar for robust HTML parsing
        
        # Look for film data patterns (this would be much more sophisticated in reality)
        # For demonstration purposes, creating some sample data
        
        # Generate some sample film data
        sample_films = [
            {"title": "The Godfather", "year": 1972, "rating": 4.5, "watched_date": "2024-01-15"},
            {"title": "Pulp Fiction", "year": 1994, "rating": 4.0, "watched_date": "2024-01-20"},
            {"title": "The Dark Knight", "year": 2008, "rating": 4.5, "watched_date": "2024-02-01"},
            {"title": "Forrest Gump", "year": 1994, "rating": 4.0, "watched_date": "2024-02-10"},
            {"title": "Inception", "year": 2010, "rating": 4.5, "watched_date": "2024-02-15"}
        ]
        
        # In a real implementation, you would:
        # 1. Parse the HTML to find film containers
        # 2. Extract film titles, years, ratings, and watch dates
        # 3. Handle pagination and rating scales
        # 4. Deal with films that don't have ratings
        
        for film in sample_films:
            films.append({
                "title": film["title"],
                "year": film["year"],
                "rating": film["rating"],
                "watched_date": film["watched_date"],
                "review_text": None,
                "is_rewatch": False,
                "letterboxd_url": f"https://letterboxd.com/film/{film['title'].lower().replace(' ', '-')}/"
            })
        
        return films
    
    def _process_and_save_films(self, user_id: int, films: List[Dict]) -> int:
        """Process scraped films and save to database"""
        processed_count = 0
        
        for film in films:
            try:
                # Find or create movie
                movie_id = self.db_manager.find_or_create_movie(
                    title=film["title"],
                    year=film["year"],
                    letterboxd_id=film.get("letterboxd_url")
                )
                
                # Save rating if present
                if film.get("rating"):
                    self.db_manager.save_rating(
                        user_id=user_id,
                        movie_id=movie_id,
                        rating=film["rating"],
                        review_text=film.get("review_text"),
                        watched_date=film["watched_date"],
                        is_rewatch=film.get("is_rewatch", False)
                    )
                    processed_count += 1
                    
            except Exception as e:
                logger.error(f"Error processing film {film.get('title', 'Unknown')}: {e}")
                continue
        
        return processed_count
    
    def _make_request(self, url: str, retries: int = 0) -> requests.Response:
        """Make HTTP request with retry logic and rate limiting"""
        try:
            # Rate limiting
            delay = random.uniform(self.min_delay, self.max_delay)
            time.sleep(delay)
            
            response = self.session.get(url, timeout=30)
            response.raise_for_status()
            
            return response
            
        except requests.exceptions.RequestException as e:
            if retries < self.max_retries:
                wait_time = self.backoff_factor ** retries
                logger.warning(f"Request failed, retrying in {wait_time}s: {e}")
                time.sleep(wait_time)
                return self._make_request(url, retries + 1)
            else:
                logger.error(f"Request failed after {self.max_retries} retries: {e}")
                raise
    
    def get_collection_stats(self) -> Dict:
        """Get statistics about data collection"""
        stats = self.db_manager.get_dataset_stats()
        
        return {
            "total_users": stats.get("total_users", 0),
            "total_movies": stats.get("total_movies", 0),
            "total_ratings": stats.get("total_ratings", 0),
            "avg_rating": float(stats.get("avg_rating", 0)) if stats.get("avg_rating") else 0,
            "last_updated": datetime.now().isoformat()
        }