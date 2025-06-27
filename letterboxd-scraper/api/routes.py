from fastapi import APIRouter, HTTPException, BackgroundTasks
from scraper.data_models import (
    ScrapeRequest, ScrapeResponse, UserProfile, 
    SearchRequest, SearchResponse, FilmSearchResult
)
from scraper.scraper_service import ScraperService
import time
import logging

# Initialize router and services
router = APIRouter()
scraper_service = ScraperService()
logger = logging.getLogger(__name__)

@router.post("/scrape/user", response_model=ScrapeResponse)
async def scrape_user_data(request: ScrapeRequest):
    """Scrape comprehensive user data from Letterboxd"""
    try:
        logger.info(f"Received scrape request for user: {request.username}")
        
        # Validate username first
        if not scraper_service.validate_user_exists(request.username):
            raise HTTPException(
                status_code=404, 
                detail=f"Letterboxd user '{request.username}' not found"
            )
        
        # Perform the scrape
        result = await scraper_service.scrape_user_complete(
            username=request.username,
            include_ratings=request.include_ratings,
            include_watchlist=request.include_watchlist,
            rating_limit=request.rating_limit
        )
        
        if not result.success:
            raise HTTPException(
                status_code=500, 
                detail=result.error_message or "Unknown error occurred during scraping"
            )
        
        logger.info(f"Successfully completed scrape for user: {request.username}")
        return result
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Unexpected error scraping user {request.username}: {e}")
        raise HTTPException(status_code=500, detail=f"Internal server error: {str(e)}")

@router.get("/user/{username}/profile", response_model=UserProfile)
async def get_user_profile(username: str):
    """Get just the user profile information"""
    try:
        logger.info(f"Fetching profile for user: {username}")
        
        # Validate username first
        if not scraper_service.validate_user_exists(username):
            raise HTTPException(
                status_code=404, 
                detail=f"Letterboxd user '{username}' not found"
            )
        
        profile = await scraper_service.scrape_user_profile_only(username)
        logger.info(f"Successfully fetched profile for user: {username}")
        return profile
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error fetching profile for {username}: {e}")
        raise HTTPException(status_code=500, detail=f"Failed to fetch user profile: {str(e)}")

@router.get("/user/{username}/validate")
async def validate_user(username: str):
    """Validate if a Letterboxd user exists"""
    try:
        exists = scraper_service.validate_user_exists(username)
        return {
            "username": username,
            "exists": exists,
            "message": f"User '{username}' {'exists' if exists else 'does not exist'} on Letterboxd"
        }
    except Exception as e:
        logger.error(f"Error validating user {username}: {e}")
        raise HTTPException(status_code=500, detail=f"Error validating user: {str(e)}")

@router.post("/search/films", response_model=SearchResponse)
async def search_films(request: SearchRequest):
    """Search for films on Letterboxd"""
    try:
        start_time = time.time()
        logger.info(f"Searching films with query: '{request.query}' (limit: {request.limit})")
        
        results_data = await scraper_service.search_films(request.query, request.limit)
        search_time = time.time() - start_time
        
        # Convert to FilmSearchResult objects
        results = [FilmSearchResult(**film) for film in results_data]
        
        response = SearchResponse(
            query=request.query,
            results=results,
            total=len(results),
            search_time=round(search_time, 3)
        )
        
        logger.info(f"Search completed: found {len(results)} films in {search_time:.3f}s")
        return response
        
    except Exception as e:
        logger.error(f"Error searching films with query '{request.query}': {e}")
        raise HTTPException(status_code=500, detail=f"Search failed: {str(e)}")

@router.get("/search/films")
async def search_films_get(q: str, limit: int = 20):
    """Search for films via GET request (alternative endpoint)"""
    request = SearchRequest(query=q, limit=limit)
    return await search_films(request)
