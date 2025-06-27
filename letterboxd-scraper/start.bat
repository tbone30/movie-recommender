@echo off
echo Starting Letterboxd Scraper Service...
echo.

REM Check if Python is installed
python --version >nul 2>&1
if errorlevel 1 (
    echo Python is not installed or not in PATH
    pause
    exit /b 1
)

REM Create virtual environment if it doesn't exist
if not exist venv (
    echo Creating virtual environment...
    python -m venv venv
)

REM Activate virtual environment
echo Activating virtual environment...
call venv\Scripts\activate.bat

REM Install dependencies
echo Installing dependencies...
pip install -r requirements.txt

REM Start the service
echo.
echo Starting Letterboxd Scraper API...
echo Service will be available at: http://localhost:5000
echo API Documentation: http://localhost:5000/docs
echo.

python app.py

pause
