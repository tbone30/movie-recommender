import requests
from bs4 import BeautifulSoup
from typing import Dict, List, Optional
import asyncio
import logging
from datetime import datetime
import re
import aiohttp
import time

class LetterboxdClient:
    def __init__(self):
        self.base_url = "https://letterboxd.com"
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
        })
        self.logger = logging.getLogger(__name__)
    
    def validate_username(self, username: str) -> bool:
        """Check if a Letterboxd user exists"""
        try:
            url = f"{self.base_url}/{username}/"
            response = self.session.get(url)
            return response.status_code == 200
        except Exception as e:
            self.logger.error(f"Error validating user {username}: {e}")
            return False
    
    async def get_user_profile(self, username: str) -> Dict:
        """Get user profile information"""
        try:
            self.logger.info(f"Fetching profile for user: {username}")
            url = f"{self.base_url}/{username}/"
            
            async with aiohttp.ClientSession() as session:
                async with session.get(url, headers=self.session.headers) as response:
                    if response.status != 200:
                        raise Exception(f"User {username} not found")
                    
                    html = await response.text()
                    soup = BeautifulSoup(html, 'html.parser')
                    
                    # Extract basic profile info
                    display_name = username
                    bio = None
                    location = None
                    website = None
                    joined_date = None
                    films_watched = 0
                    followers = 0
                    following = 0
                    
                    # Try to extract display name
                    display_name_elem = soup.find('h1', class_='title-1')
                    if display_name_elem:
                        display_name = display_name_elem.get_text(strip=True)
                    
                    # Try to extract bio
                    bio_elem = soup.find('div', class_='profile-text')
                    if bio_elem:
                        bio = bio_elem.get_text(strip=True)
                    
                    # Try to extract stats
                    stats_section = soup.find('section', class_='section stats')
                    if stats_section:
                        stat_links = stats_section.find_all('a', class_='stat')
                        for stat_link in stat_links:
                            stat_value_elem = stat_link.find('span', class_='value')
                            if stat_value_elem:
                                stat_value = stat_value_elem.get_text(strip=True).replace(',', '')
                                href = stat_link.get('href', '')
                                if 'films' in href:
                                    try:
                                        films_watched = int(stat_value)
                                    except ValueError:
                                        pass
                                elif 'followers' in href:
                                    try:
                                        followers = int(stat_value)
                                    except ValueError:
                                        pass
                                elif 'following' in href:
                                    try:
                                        following = int(stat_value)
                                    except ValueError:
                                        pass
                    
                    profile_data = {
                        'username': username,
                        'display_name': display_name,
                        'bio': bio,
                        'location': location,
                        'website': website,
                        'joined_date': joined_date,
                        'films_watched': films_watched,
                        'followers': followers,
                        'following': following
                    }
                    
                    self.logger.info(f"Successfully fetched profile for user: {username}")
                    return profile_data
            
        except Exception as e:
            self.logger.error(f"Error fetching user {username}: {e}")
            raise Exception(f"Failed to fetch user profile: {str(e)}")
    
    async def get_user_ratings(self, username: str, limit: int = 100) -> List[Dict]:
        """Get user's film ratings"""
        try:
            self.logger.info(f"Fetching ratings for user: {username} (limit: {limit})")
            
            ratings = []
            page = 1
            collected = 0
            
            while collected < limit:
                url = f"{self.base_url}/{username}/films/page/{page}/"
                
                async with aiohttp.ClientSession() as session:
                    async with session.get(url, headers=self.session.headers) as response:
                        if response.status != 200:
                            break
                        
                        html = await response.text()
                        soup = BeautifulSoup(html, 'html.parser')
                        
                        # Find film entries
                        film_items = soup.find_all('li', class_='poster-container')
                        
                        if not film_items:
                            break
                        
                        for item in film_items:
                            if collected >= limit:
                                break
                            
                            try:
                                # Extract film data
                                img_elem = item.find('img')
                                if not img_elem:
                                    continue
                                
                                film_title = img_elem.get('alt', 'Unknown')
                                film_slug = ''
                                film_year = None
                                rating = None
                                
                                # Extract year from title if present
                                year_match = re.search(r'\((\d{4})\)', film_title)
                                if year_match:
                                    film_year = int(year_match.group(1))
                                    film_title = film_title.replace(f' ({film_year})', '')
                                
                                # Try to get rating
                                rating_elem = item.find('span', class_='rating')
                                if rating_elem and rating_elem.get('class'):
                                    rating_classes = rating_elem.get('class', [])
                                    for cls in rating_classes:
                                        if cls.startswith('rated-'):
                                            try:
                                                rating = int(cls.replace('rated-', '')) / 2.0
                                            except ValueError:
                                                pass
                                
                                # Get film link for slug
                                link_elem = item.find('a')
                                if link_elem:
                                    href = link_elem.get('href', '')
                                    if href.startswith('/film/'):
                                        film_slug = href.replace('/film/', '').rstrip('/')
                                
                                rating_data = {
                                    'film_title': film_title,
                                    'film_year': film_year,
                                    'film_slug': film_slug,
                                    'rating': rating,
                                    'watched_date': None,
                                    'review': None,
                                    'letterboxd_uri': f"{self.base_url}/film/{film_slug}/" if film_slug else None
                                }
                                
                                ratings.append(rating_data)
                                collected += 1
                                
                            except Exception as e:
                                self.logger.warning(f"Error processing film item: {e}")
                                continue
                
                page += 1
                # Add delay to be respectful
                await asyncio.sleep(0.5)
            
            self.logger.info(f"Successfully fetched {len(ratings)} ratings for user: {username}")
            return ratings[:limit]
            
        except Exception as e:
            self.logger.error(f"Error fetching ratings for user {username}: {e}")
            return []
    
    async def get_user_watchlist(self, username: str) -> List[Dict]:
        """Get user's watchlist"""
        try:
            self.logger.info(f"Fetching watchlist for user: {username}")
            url = f"{self.base_url}/{username}/watchlist/"
            
            watchlist = []
            
            async with aiohttp.ClientSession() as session:
                async with session.get(url, headers=self.session.headers) as response:
                    if response.status != 200:
                        return []
                    
                    html = await response.text()
                    soup = BeautifulSoup(html, 'html.parser')
                    
                    # Find film entries
                    film_items = soup.find_all('li', class_='poster-container')
                    
                    for item in film_items:
                        try:
                            # Extract film data
                            img_elem = item.find('img')
                            if not img_elem:
                                continue
                            
                            film_title = img_elem.get('alt', 'Unknown')
                            film_slug = ''
                            film_year = None
                            
                            # Extract year from title if present
                            year_match = re.search(r'\((\d{4})\)', film_title)
                            if year_match:
                                film_year = int(year_match.group(1))
                                film_title = film_title.replace(f' ({film_year})', '')
                            
                            # Get film link for slug
                            link_elem = item.find('a')
                            if link_elem:
                                href = link_elem.get('href', '')
                                if href.startswith('/film/'):
                                    film_slug = href.replace('/film/', '').rstrip('/')
                            
                            watchlist_data = {
                                'film_title': film_title,
                                'film_year': film_year,
                                'film_slug': film_slug,
                                'directors': [],
                                'genres': [],
                                'added_date': None,
                                'letterboxd_uri': f"{self.base_url}/film/{film_slug}/" if film_slug else None
                            }
                            
                            watchlist.append(watchlist_data)
                            
                        except Exception as e:
                            self.logger.warning(f"Error processing watchlist item: {e}")
                            continue
            
            self.logger.info(f"Successfully fetched {len(watchlist)} watchlist items for user: {username}")
            return watchlist
            
        except Exception as e:
            self.logger.error(f"Error fetching watchlist for user {username}: {e}")
            return []
    
    async def search_films(self, query: str, limit: int = 20) -> List[Dict]:
        """Search for films"""
        try:
            self.logger.info(f"Searching films with query: {query}")
            url = f"{self.base_url}/search/films/{query}/"
            
            results = []
            
            async with aiohttp.ClientSession() as session:
                async with session.get(url, headers=self.session.headers) as response:
                    if response.status != 200:
                        return []
                    
                    html = await response.text()
                    soup = BeautifulSoup(html, 'html.parser')
                    
                    # Find film results
                    film_items = soup.find_all('li', class_='listitem')[:limit]
                    
                    for item in film_items:
                        try:
                            # Extract film data
                            title_elem = item.find('h2')
                            if not title_elem:
                                continue
                            
                            title_link = title_elem.find('a')
                            if not title_link:
                                continue
                            
                            film_title = title_link.get_text(strip=True)
                            film_slug = ''
                            film_year = None
                            
                            # Get film link for slug
                            href = title_link.get('href', '')
                            if href.startswith('/film/'):
                                film_slug = href.replace('/film/', '').rstrip('/')
                            
                            # Try to get year
                            year_elem = item.find('small')
                            if year_elem:
                                year_text = year_elem.get_text(strip=True)
                                year_match = re.search(r'(\d{4})', year_text)
                                if year_match:
                                    film_year = int(year_match.group(1))
                            
                            search_result = {
                                'title': film_title,
                                'year': film_year,
                                'slug': film_slug,
                                'directors': [],
                                'genres': [],
                                'average_rating': None,
                                'rating_count': None,
                                'letterboxd_uri': f"{self.base_url}/film/{film_slug}/" if film_slug else None
                            }
                            
                            results.append(search_result)
                            
                        except Exception as e:
                            self.logger.warning(f"Error processing search result: {e}")
                            continue
            
            self.logger.info(f"Search completed: found {len(results)} films")
            return results
            
        except Exception as e:
            self.logger.error(f"Error searching films: {e}")
            return []
