from pydantic import BaseModel, Field
from typing import List, Optional
from datetime import datetime

class UserProfile(BaseModel):
    username: str
    display_name: Optional[str] = None
    bio: Optional[str] = None
    location: Optional[str] = None
    website: Optional[str] = None
    joined_date: Optional[str] = None
    films_watched: Optional[int] = None
    followers: Optional[int] = None
    following: Optional[int] = None

class FilmRating(BaseModel):
    film_title: str
    film_year: Optional[int] = None
    film_slug: str
    rating: Optional[float] = Field(None, ge=0, le=5)
    watched_date: Optional[str] = None
    review: Optional[str] = None
    letterboxd_uri: Optional[str] = None

class WatchlistFilm(BaseModel):
    film_title: str
    film_year: Optional[int] = None
    film_slug: str
    directors: List[str] = []
    genres: List[str] = []
    added_date: Optional[str] = None
    letterboxd_uri: Optional[str] = None

class FilmSearchResult(BaseModel):
    title: str
    year: Optional[int] = None
    slug: str
    directors: List[str] = []
    genres: List[str] = []
    average_rating: Optional[float] = None
    rating_count: Optional[int] = None
    letterboxd_uri: Optional[str] = None

class ScrapeRequest(BaseModel):
    username: str
    include_ratings: bool = True
    include_watchlist: bool = True
    rating_limit: int = Field(default=100, ge=1, le=1000)

class ScrapeResponse(BaseModel):
    username: str
    profile: UserProfile
    ratings: List[FilmRating] = []
    watchlist: List[WatchlistFilm] = []
    scraped_at: datetime
    total_ratings: int
    total_watchlist_items: int
    success: bool = True
    error_message: Optional[str] = None

class HealthResponse(BaseModel):
    status: str
    service: str
    version: str
    timestamp: datetime

class SearchRequest(BaseModel):
    query: str
    limit: int = Field(default=20, ge=1, le=100)

class SearchResponse(BaseModel):
    query: str
    results: List[FilmSearchResult]
    total: int
    search_time: float
