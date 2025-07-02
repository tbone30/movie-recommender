import requests
import logging
from letterboxdpy import user
from letterboxdpy import movie

class LetterboxdClient:
    def __init__(self):
        self.base_url = "https://letterboxd.com"
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
        })
        self.logger = logging.getLogger(__name__)
    
    def get_user(self, username: str):
        user_instance = user.User(username)
        return user_instance
    
    def get_user_films_rated(self, username: str):
        user_instance = user.User(username)
        films = user_instance.user_films_rated()
        return films
    
    def get_user_watchlist(self, username: str):
        user_instance = user.User(username)
        watchlist = user_instance.user_watchlist()
        return watchlist
    
    def get_user_genre_info(self, username: str):
        user_instance = user.User(username)
        genre_info = user_instance.user_genre_info()
        return genre_info

    def get_movie(self, slug: str):
        movie_instance = movie.Movie(slug)
        return movie_instance
    
    def get_movie_popular_reviews(self, slug: str):
        movie_instance = movie.Movie(slug)
        reviews = movie.popular_reviews(movie_instance)
        return reviews
    
    def get_movie_tmdb_link(self, slug: str):
        movie_instance = movie.Movie(slug)
        tmdb_link = movie_instance.tmdb_link()
        return tmdb_link

    def get_movie_description(self, slug: str):
        movie_instance = movie.Movie(slug)
        description = movie_instance.description()
        return description
    
    def get_movie_poster(self, slug: str):
        movie_instance = movie.Movie(slug)
        poster = movie_instance.poster()
        return poster

if __name__ == "__main__":
    client = LetterboxdClient()
    client.get_user("thomas_welch")
    client.get_movie("inception")
    
