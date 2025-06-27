from scraper.letterboxd_client import LetterboxdClient
from scraper.data_models import UserProfile, FilmRating, WatchlistFilm, ScrapeResponse
from datetime import datetime
import logging

class ScraperService:
    def __init__(self):
        self.client = LetterboxdClient()
        self.logger = logging.getLogger(__name__)
    
    async def scrape_user_complete(self, username: str, include_ratings: bool = True, 
                                 include_watchlist: bool = True, rating_limit: int = 100) -> ScrapeResponse:
        """Scrape complete user data from Letterboxd"""
        try:
            self.logger.info(f"Starting complete scrape for user: {username}")
            
            # Get user profile
            profile_data = await self.client.get_user_profile(username)
            profile = UserProfile(**profile_data)
            
            ratings = []
            watchlist = []
            
            # Get ratings if requested
            if include_ratings:
                try:
                    ratings_data = await self.client.get_user_ratings(username, limit=rating_limit)
                    ratings = [FilmRating(**rating) for rating in ratings_data]
                except Exception as e:
                    self.logger.warning(f"Failed to fetch ratings for {username}: {e}")
            
            # Get watchlist if requested
            if include_watchlist:
                try:
                    watchlist_data = await self.client.get_user_watchlist(username)
                    watchlist = [WatchlistFilm(**film) for film in watchlist_data]
                except Exception as e:
                    self.logger.warning(f"Failed to fetch watchlist for {username}: {e}")
            
            response = ScrapeResponse(
                username=username,
                profile=profile,
                ratings=ratings,
                watchlist=watchlist,
                scraped_at=datetime.now(),
                total_ratings=len(ratings),
                total_watchlist_items=len(watchlist),
                success=True
            )
            
            self.logger.info(f"Complete scrape finished for {username}: {len(ratings)} ratings, {len(watchlist)} watchlist items")
            return response
            
        except Exception as e:
            self.logger.error(f"Error in complete scrape for user {username}: {e}")
            return ScrapeResponse(
                username=username,
                profile=UserProfile(username=username),
                ratings=[],
                watchlist=[],
                scraped_at=datetime.now(),
                total_ratings=0,
                total_watchlist_items=0,
                success=False,
                error_message=str(e)
            )
    
    async def scrape_user_profile_only(self, username: str) -> UserProfile:
        """Scrape only user profile information"""
        try:
            profile_data = await self.client.get_user_profile(username)
            return UserProfile(**profile_data)
        except Exception as e:
            self.logger.error(f"Error fetching profile for {username}: {e}")
            raise
    
    async def search_films(self, query: str, limit: int = 20):
        """Search for films"""
        try:
            return await self.client.search_films(query, limit)
        except Exception as e:
            self.logger.error(f"Error searching films: {e}")
            raise
    
    def validate_user_exists(self, username: str) -> bool:
        """Check if a Letterboxd user exists"""
        return self.client.validate_username(username)
