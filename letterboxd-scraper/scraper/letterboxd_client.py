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
        films = user.user_films_rated(user_instance)
        return films
    
    def get_user_watchlist(self, username: str):
        user_instance = user.User(username)
        watchlist = user_instance.user_watchlist()
        return watchlist
    
    def get_user_genre_info(self, username: str):
        user_instance = user.User(username)
        genre_info = user.user_genre_info(user_instance)
        return genre_info

    def get_movie(self, slug: str):
        movie_instance = movie.Movie(slug)
        return movie_instance
    
    def get_movie_tmdb_link(self, slug: str):
        movie_instance = movie.Movie(slug)
        tmdb_link = movie.movie_tmdb_link(movie_instance)
        return tmdb_link

    def get_movie_description(self, slug: str):
        movie_instance = movie.Movie(slug)
        description = movie.movie_description(movie_instance)
        return description
    
    def get_movie_poster(self, slug: str):
        movie_instance = movie.Movie(slug)
        poster = movie.movie_poster(movie_instance)
        return poster

if __name__ == "__main__":
    client = LetterboxdClient()
    print(client.get_user("thomas_welch"))
    print(client.get_movie("inception"))
    