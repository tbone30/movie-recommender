# Letterboxd Scraper Service

A Python microservice for scraping Letterboxd user data using the `letterboxdpy` library.

## Features

- **User Profile Scraping**: Get comprehensive user profile information
- **Ratings Extraction**: Fetch user's film ratings and reviews
- **Watchlist Access**: Retrieve user's watchlist items
- **Film Search**: Search for films in the Letterboxd database
- **RESTful API**: Clean REST API endpoints for integration
- **Error Handling**: Robust error handling and logging
- **CORS Support**: Configured for cross-origin requests

## API Endpoints

### Health Check
- `GET /health` - Service health check

### User Operations
- `POST /api/scrape/user` - Scrape complete user data
- `GET /api/user/{username}/profile` - Get user profile only
- `GET /api/user/{username}/validate` - Validate if user exists

### Film Search
- `POST /api/search/films` - Search for films
- `GET /api/search/films?q={query}` - Search films via GET

## Installation

1. Install dependencies:
```bash
pip install -r requirements.txt
```

2. Set up environment variables (optional):
```bash
cp .env.example .env
# Edit .env with your preferred settings
```

3. Run the service:
```bash
python app.py
```

The service will start on `http://localhost:5000` by default.

## Usage Examples

### Scrape User Data
```bash
curl -X POST "http://localhost:5000/api/scrape/user" \
     -H "Content-Type: application/json" \
     -d '{
       "username": "letterboxd_username",
       "include_ratings": true,
       "include_watchlist": true,
       "rating_limit": 100
     }'
```

### Get User Profile
```bash
curl "http://localhost:5000/api/user/letterboxd_username/profile"
```

### Search Films
```bash
curl "http://localhost:5000/api/search/films?q=inception&limit=10"
```

## Configuration

The service can be configured using environment variables in the `.env` file:

- `SCRAPER_HOST`: Host to bind to (default: 0.0.0.0)
- `SCRAPER_PORT`: Port to bind to (default: 5000)
- `LOG_LEVEL`: Logging level (default: INFO)
- `ALLOWED_ORIGINS`: CORS allowed origins (default: http://localhost:8080,http://localhost:3000)

## Integration

This service is designed to work with the Spring Boot backend. The main application will communicate with this service via HTTP requests to scrape and retrieve Letterboxd data.

## Development

Run tests:
```bash
python -m pytest tests/ -v
```

Run with auto-reload for development:
```bash
uvicorn app:app --reload --host 0.0.0.0 --port 5000
```

## Dependencies

- `letterboxdpy`: Letterboxd API wrapper
- `fastapi`: Modern web framework
- `uvicorn`: ASGI server
- `pydantic`: Data validation

## Notes

- Respects Letterboxd's rate limiting
- Handles errors gracefully
- Logs all operations for debugging
- Designed for microservice architecture
