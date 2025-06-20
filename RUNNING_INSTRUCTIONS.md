# Movie Recommender System - Running Instructions

This guide provides step-by-step instructions for setting up and running the Movie Recommender System on your local machine.

## 📋 Prerequisites

Before starting, ensure you have the following installed:

- **Java 17 or higher** - [Download here](https://adoptium.net/)
- **Node.js 16+ and npm** - [Download here](https://nodejs.org/)
- **Python 3.9 or higher** - [Download here](https://www.python.org/downloads/)
- **Maven 3.6+** - [Download here](https://maven.apache.org/download.cgi)
- **Git** - [Download here](https://git-scm.com/)

### Optional (for better performance)
- **Redis 6+** - [Download here](https://redis.io/download) or use Docker (optional, will use in-memory cache if not available)

### API Keys Required
- **TMDb API Key** - Get from [The Movie Database](https://www.themoviedb.org/settings/api)

> **Note**: This setup uses SQLite as the database, which is file-based and doesn't require a separate database server installation, making it much simpler to run!

## 🚀 Quick Setup Guide

### Step 1: Clone the Repository

```powershell
git clone https://github.com/yourusername/movie-recommender.git
cd movie-recommender
```

### Step 2: Optional Redis Setup (Recommended)

Redis provides better caching performance, but the application will work without it using in-memory caching.

**Option A: Use Docker (Recommended)**
```powershell
docker run --name redis-movie-recommender -p 6379:6379 -d redis:alpine
```

**Option B: Skip Redis**
The application will automatically use in-memory caching if Redis is not available.

### Step 3: Backend Configuration

1. **Navigate to backend directory**
   ```powershell
   cd backend
   ```

2. **Configure application properties**
   
   Update `src/main/resources/application.properties`:
   ```properties
   # Database Configuration (SQLite - no setup required!)
   spring.datasource.url=jdbc:sqlite:./data/movierecommender.db
   spring.datasource.driver-class-name=org.sqlite.JDBC
   
   # TMDb API Configuration
   tmdb.api.key=YOUR_TMDB_API_KEY_HERE
   
   # JWT Configuration
   app.jwt.secret=your-256-bit-secret-key-here
   app.jwt.expiration=86400000
   
   # Redis Configuration (optional)
   spring.data.redis.host=localhost
   spring.data.redis.port=6379
   
   # ML Service Configuration
   ml.service.url=http://localhost:5000
   ```

3. **Install dependencies and build**
   ```powershell
   mvn clean install
   ```

### Step 4: Python ML Service Setup

1. **Navigate to ML service directory**
   ```powershell
   cd ..\python-ml-service
   ```

2. **Create virtual environment (recommended)**
   ```powershell
   python -m venv venv
   venv\Scripts\Activate.ps1
   ```

3. **Install dependencies**
   ```powershell
   pip install -r requirements.txt
   ```

4. **Configure environment variables**
   
   Create `.env` file in `python-ml-service` directory:
   ```env
   DATABASE_URL=sqlite:///./data/movierecommender.db
   FLASK_ENV=development
   FLASK_DEBUG=True
   PORT=5000
   ```

### Step 5: Frontend Setup

1. **Navigate to frontend directory**
   ```powershell
   cd ..\frontend
   ```

2. **Install dependencies**
   ```powershell
   npm install
   ```

3. **Configure environment variables**
   
   Create `.env` file in `frontend` directory:
   ```env
   REACT_APP_API_URL=http://localhost:8080/api
   REACT_APP_ML_SERVICE_URL=http://localhost:5000
   ```

## 🏃‍♂️ Running the Application

### Start All Services

You'll need **3 terminal windows** to run all services (Redis is optional):

#### Terminal 1: Backend (Spring Boot)
```powershell
cd backend
mvn spring-boot:run
```
**Backend will be available at:** http://localhost:8080

#### Terminal 2: Python ML Service
```powershell
cd python-ml-service
# Activate virtual environment if using one
venv\Scripts\Activate.ps1
python app.py
```
**ML Service will be available at:** http://localhost:5000

#### Terminal 3: Frontend (React)
```powershell
cd frontend
npm start
```
**Frontend will be available at:** http://localhost:3000

#### Optional Terminal 4: Redis (for better performance)
```powershell
# If using Docker
docker start redis-movie-recommender

# Or if installed locally
redis-server
```

### Verify Services
```powershell
### Verify Services

1. **Backend Health Check**
   ```powershell
   curl http://localhost:8080/actuator/health
   ```

2. **ML Service Health Check**
   ```powershell
   curl http://localhost:5000/health
   ```

3. **Frontend** - Open browser to http://localhost:3000

4. **Database Creation Check**
   ```powershell
   # Check if SQLite database was created automatically
   if (Test-Path "backend\data\movierecommender.db") {
       Write-Host "✅ SQLite database created successfully!"
       $dbFile = Get-Item "backend\data\movierecommender.db"
       Write-Host "Database size: $([math]::Round($dbFile.Length / 1KB, 2)) KB"
   } else {
       Write-Host "⏳ Database will be created on first API call"
   }
   ```

## 🗂️ Service Overview

| Service | Port | URL | Purpose |
|---------|------|-----|---------|
| Frontend | 3000 | http://localhost:3000 | React web interface |
| Backend | 8080 | http://localhost:8080 | Spring Boot REST API |
| ML Service | 5000 | http://localhost:5000 | Python ML algorithms |
| SQLite Database | - | ./data/movierecommender.db | File-based database |
| Redis (Optional) | 6379 | localhost:6379 | Caching (optional) |

## 🔧 Troubleshooting

### Common Issues

#### 1. Database Issues
```powershell
# Check if SQLite database file exists
if (Test-Path "backend\data\movierecommender.db") {
    Write-Host "Database file exists"
} else {
    Write-Host "Database file will be created on first run"
}
```

#### 2. Port Already in Use
```powershell
# Find process using port 8080
netstat -ano | findstr :8080

# Kill process (replace PID with actual process ID)
taskkill /PID <PID> /F
```

#### 3. Python Dependencies Issues
```powershell
# Upgrade pip first
python -m pip install --upgrade pip

# Install dependencies with verbose output
pip install -r requirements.txt -v
```

#### 4. Node.js/npm Issues
```powershell
# Clear npm cache
npm cache clean --force

# Delete node_modules and reinstall
Remove-Item -Recurse -Force node_modules
npm install
```

#### 5. Redis Connection Issues (Optional)
```powershell
# Test Redis connection (only if using Redis)
redis-cli ping
# Should return "PONG"
```

> **Note**: If Redis is not available, the application will automatically fall back to in-memory caching.

### Environment Variables Check

Create a script to verify all environment variables are set:

```powershell
# check-env.ps1
Write-Host "Checking Backend Environment..."
Get-Content backend\src\main\resources\application.properties | Select-String "tmdb.api.key"

Write-Host "Checking ML Service Environment..."
if (Test-Path python-ml-service\.env) {
    Get-Content python-ml-service\.env
} else {
    Write-Host "ML Service .env file not found!"
}

Write-Host "Checking Frontend Environment..."
if (Test-Path frontend\.env) {
    Get-Content frontend\.env
} else {
    Write-Host "Frontend .env file not found!"
}

Write-Host "Checking Database..."
if (Test-Path backend\data\movierecommender.db) {
    Write-Host "SQLite database file exists"
} else {
    Write-Host "SQLite database will be created on first run"
}
```

## 📊 First Time Setup

### 1. Create Admin User
After starting all services, register the first user through the frontend, then manually promote to admin:

```powershell
# Install SQLite command line tool if not already installed
# Or use DB Browser for SQLite (https://sqlitebrowser.org/)

# Connect to SQLite database
sqlite3 backend\data\movierecommender.db

# Find user and update role
UPDATE users SET role = 'ADMIN' WHERE email = 'your-email@example.com';

# Exit SQLite
.quit
```

### 2. Initial Data Setup
1. Login to the admin dashboard at http://localhost:3000/admin
2. Use the "Rebuild Dataset" feature to initialize the system
3. Optionally sync data from Letterboxd profiles

## 🔄 Development Workflow

### Hot Reload Development
- **Frontend**: Changes automatically reload with React hot reload
- **Backend**: Use `mvn spring-boot:run` with dev tools for automatic restart
- **ML Service**: Restart manually when making changes

### Database Migrations
The application uses Hibernate with `ddl-auto=update`, so schema changes are applied automatically.

### Logs Location
- **Backend**: Console output and `backend/logs/application.log`
- **ML Service**: Console output
- **Frontend**: Browser console and terminal

## 🧪 Testing the Setup

### 1. Registration and Login
1. Go to http://localhost:3000
2. Register a new account
3. Login with your credentials

### 2. Add Some Ratings
1. Navigate to Movies page
2. Search for movies and add ratings
3. Rate at least 10 movies for better recommendations

### 3. Generate Recommendations
1. Go to Recommendations page
2. Click "Generate New Recommendations"
3. Wait for the ML service to process

### 4. Test Letterboxd Sync (Optional)
1. Go to Profile page
2. Enter a Letterboxd username
3. Click "Sync from Letterboxd"

## 📈 Performance Tips

1. **Use Redis**: Ensure Redis is running for optimal performance
2. **Database Indexing**: The application creates necessary indexes automatically
3. **ML Model Caching**: Models are cached after first training
4. **Frontend Optimization**: Use production build for deployment

## 🔒 Security Notes

- Change default passwords in production
- Use environment variables for sensitive data
- Enable HTTPS in production
- Review CORS settings before deployment

## 🆘 Getting Help

If you encounter issues:

1. Check the logs in each service
2. Verify all prerequisites are installed
3. Ensure all environment variables are set correctly
4. Check that all ports are available
5. Refer to the main README.md for additional documentation

## 🔄 Stopping the Application

To properly stop all services:

1. **Frontend**: Press `Ctrl+C` in the React terminal
2. **Backend**: Press `Ctrl+C` in the Spring Boot terminal
3. **ML Service**: Press `Ctrl+C` in the Python terminal
4. **Redis**: If using Docker: `docker stop redis-movie-recommender`

---

**Happy coding! 🎬✨**

## 🎯 Why SQLite?

This setup uses SQLite instead of PostgreSQL for several advantages:

- ✅ **No database server setup required** - SQLite is file-based
- ✅ **Simpler development** - Database file travels with your project
- ✅ **Perfect for learning** - Focus on the app, not database administration
- ✅ **Production ready** - Handles thousands of users efficiently
- ✅ **Zero configuration** - Just run the app and the database is created automatically

The SQLite database file will be automatically created at `backend/data/movierecommender.db` when you first start the application!
