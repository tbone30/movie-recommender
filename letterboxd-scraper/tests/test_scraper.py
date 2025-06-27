import unittest
from unittest.mock import Mock, patch
import sys
import os

# Add the parent directory to the path so we can import our modules
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

class TestLetterboxdScraper(unittest.TestCase):
    
    def setUp(self):
        """Set up test fixtures before each test method."""
        pass
    
    def test_user_profile_structure(self):
        """Test that user profile data structure is correct"""
        from scraper.data_models import UserProfile
        
        profile_data = {
            'username': 'testuser',
            'display_name': 'Test User',
            'bio': 'Test bio',
            'films_watched': 100
        }
        
        profile = UserProfile(**profile_data)
        self.assertEqual(profile.username, 'testuser')
        self.assertEqual(profile.display_name, 'Test User')
        self.assertEqual(profile.films_watched, 100)
    
    def test_film_rating_structure(self):
        """Test that film rating data structure is correct"""
        from scraper.data_models import FilmRating
        
        rating_data = {
            'film_title': 'Test Movie',
            'film_year': 2023,
            'film_slug': 'test-movie',
            'rating': 4.5
        }
        
        rating = FilmRating(**rating_data)
        self.assertEqual(rating.film_title, 'Test Movie')
        self.assertEqual(rating.rating, 4.5)
    
    def test_scrape_request_validation(self):
        """Test scrape request validation"""
        from scraper.data_models import ScrapeRequest
        
        # Valid request
        request = ScrapeRequest(username='testuser')
        self.assertEqual(request.username, 'testuser')
        self.assertTrue(request.include_ratings)
        self.assertEqual(request.rating_limit, 100)
        
        # Custom request
        custom_request = ScrapeRequest(
            username='testuser2',
            include_ratings=False,
            rating_limit=50
        )
        self.assertFalse(custom_request.include_ratings)
        self.assertEqual(custom_request.rating_limit, 50)

if __name__ == '__main__':
    unittest.main()
