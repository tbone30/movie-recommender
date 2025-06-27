#!/bin/bash

echo "Starting Letterboxd Scraper Service..."
echo

# Check if Python is installed
if ! command -v python3 &> /dev/null; then
    echo "Python 3 is not installed or not in PATH"
    exit 1
fi

# Create virtual environment if it doesn't exist
if [ ! -d "venv" ]; then
    echo "Creating virtual environment..."
    python3 -m venv venv
fi

# Activate virtual environment
echo "Activating virtual environment..."
source venv/bin/activate

# Install dependencies
echo "Installing dependencies..."
pip install fastapi uvicorn requests python-dotenv aiohttp python-multipart beautifulsoup4 selenium pydantic

# Start the service
echo
echo "Starting Letterboxd Scraper API..."
echo "Service will be available at: http://localhost:5000"
echo "API Documentation: http://localhost:5000/docs"
echo

python app.py
