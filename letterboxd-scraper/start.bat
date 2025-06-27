@echo off
echo Starting Letterboxd Scraper Service...
echo.

REM Set Python executable path
set PYTHON_EXE=C:/Users/twelc/AppData/Local/Programs/Python/Python313/python.exe

REM Check if Python is installed
%PYTHON_EXE% --version >nul 2>&1
if errorlevel 1 (
    echo Python is not installed or not accessible at %PYTHON_EXE%
    pause
    exit /b 1
)

REM Install dependencies if needed
echo Checking and installing dependencies...
%PYTHON_EXE% -m pip install fastapi uvicorn requests python-dotenv aiohttp python-multipart beautifulsoup4 selenium pydantic

REM Start the service
echo.
echo Starting Letterboxd Scraper API...
echo Service will be available at: http://localhost:5000
echo API Documentation: http://localhost:5000/docs
echo.

%PYTHON_EXE% app.py

pause
