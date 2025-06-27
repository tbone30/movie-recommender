import os
from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv()

class Settings:
    # Server Configuration
    HOST: str = os.getenv("SCRAPER_HOST", "0.0.0.0")
    PORT: int = int(os.getenv("SCRAPER_PORT", "5000"))
    
    # Logging
    LOG_LEVEL: str = os.getenv("LOG_LEVEL", "INFO")
    
    # CORS
    ALLOWED_ORIGINS: list = os.getenv("ALLOWED_ORIGINS", "http://localhost:8080,http://localhost:3000").split(",")
    
    # Rate Limiting
    RATE_LIMIT_PER_MINUTE: int = int(os.getenv("RATE_LIMIT_PER_MINUTE", "60"))
      # API Configuration
    API_TITLE: str = "Letterboxd Scraper API"
    API_VERSION: str = "1.0.0"
    API_DESCRIPTION: str = "A microservice for scraping Letterboxd user data using web scraping techniques"

settings = Settings()
