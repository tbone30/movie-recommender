from fastapi import APIRouter, HTTPException, BackgroundTasks
from scraper.data_models import (
    ScrapeRequest, ScrapeResponse, UserProfile, 
    SearchRequest, SearchResponse, FilmSearchResult, 
    MovieProfile
)
from scraper.scraper_service import ScraperService
import time
import logging
from scraper.letterboxd_client import LetterboxdClient

# Initialize router and services
router = APIRouter()
scraper_service = ScraperService()
letterboxd_client = LetterboxdClient()
logger = logging.getLogger(__name__)

@router.get("/user/{username}", response_model=UserProfile)
async def get_user_profile(username: str):
    user_profile = letterboxd_client.get_user(username)
    if not user_profile:
        raise HTTPException(status_code=404, detail="User not found")
    return user_profile

@router.get("/user/{username}/films-rated", response_model=list)
async def get_user_films_rated(username: str):
    films = letterboxd_client.get_user_films_rated(username)
    if not films:
        raise HTTPException(status_code=404, detail="No films found")
    return films

@router.get("/user/{username}/watchlist", response_model=list)
async def get_user_watchlist(username: str):
    watchlist = letterboxd_client.get_user_watchlist(username)
    if not watchlist:
        raise HTTPException(status_code=404, detail="No watchlist found")
    return watchlist

@router.get("/user/{username}/genre-info", response_model=dict)
async def get_user_genre_info(username: str):  
    genre_info = letterboxd_client.get_user_genre_info(username)
    if not genre_info:
        raise HTTPException(status_code=404, detail="No genre info found")
    return genre_info

@router.get("/movie/{slug}", response_model=MovieProfile)
async def get_movie(slug: str):
    movie_profile = letterboxd_client.get_movie(slug)
    if not movie_profile:
        raise HTTPException(status_code=404, detail="Movie not found")
    return movie_profile

@router.get("/movie/{slug}/tmdb-link", response_model=str)
async def get_movie_tmdb_link(slug: str):
    tmdb_link = letterboxd_client.get_movie_tmdb_link(slug)
    if not tmdb_link:
        raise HTTPException(status_code=404, detail="TMDB link not found")
    return tmdb_link

@router.get("/movie/{slug}/description", response_model=list)
async def get_movie_description(slug: str):
    description = letterboxd_client.get_movie_description(slug)
    if not description:
        raise HTTPException(status_code=404, detail="Description not found")
    return description

@router.get("/movie/{slug}/poster", response_model=str)
async def get_movie_poster(slug: str):
    poster = letterboxd_client.get_movie_poster(slug)
    if not poster:
        raise HTTPException(status_code=404, detail="Poster not found")
    return poster

