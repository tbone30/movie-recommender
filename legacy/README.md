# Movie Recommender System

A comprehensive movie recommendation system built with Spring Boot, Python ML services, and modern web technologies. The system integrates with Letterboxd for user data collection and uses multiple machine learning algorithms for personalized recommendations.

## 🚀 Features

### Core Functionality
- **User Authentication & Authorization** - JWT-based secure authentication
- **Letterboxd Integration** - Sync user movie ratings from Letterboxd profiles
- **Multiple ML Algorithms** - Collaborative filtering, content-based, and hybrid recommendations
- **Real-time Recommendations** - Generate personalized movie suggestions
- **Rating Management** - Add, update, and manage movie ratings
- **Admin Dashboard** - System monitoring and management tools

### Machine Learning Capabilities
- **Collaborative Filtering** - SVD and NMF matrix factorization
- **Content-Based Filtering** - Genre, director, and metadata analysis
- **Hybrid Approach** - Combines multiple algorithms for better accuracy
- **Cold Start Handling** - Recommendations for new users
- **Model Retraining** - Automatic model updates with new data

### Technical Features
- **Microservices Architecture** - Separate backend and ML services
- **PostgreSQL Database** - Robust data storage with JPA/Hibernate
- **Redis Caching** - Performance optimization
- **TMDb Integration** - Movie metadata enrichment
- **Heroku Deployment** - Cloud-ready configuration
- **REST API** - Comprehensive API endpoints

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │  Spring Boot    │    │  Python ML      │
│   (React/Vue)   │◄──►│   Backend       │◄──►│   Service       │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │                        │
                              ▼                        ▼
                       ┌─────────────┐         ┌─────────────┐
                       │ PostgreSQL  │         │   Redis     │
                       │  Database   │         │   Cache     │
                       └─────────────┘         └─────────────┘
```

## 🛠️ Technology Stack

### Backend (Spring Boot)
- **Java 17+**
- **Spring Boot 3.x**
- **Spring Security** - JWT authentication
- **Spring Data JPA** - Database operations
- **PostgreSQL** - Primary database
- **Redis** - Caching layer
- **Maven** - Dependency management

### ML Service (Python)
- **Python 3.9+**
- **Flask** - Web framework
- **scikit-learn** - Machine learning algorithms
- **pandas/numpy** - Data processing
- **PostgreSQL** - Database integration
- **Gunicorn** - WSGI server

### External Integrations
- **TMDb API** - Movie metadata
- **Letterboxd** - User data collection
- **Heroku** - Cloud deployment

## 📋 Prerequisites

- Java 17 or higher
- Python 3.9 or higher
- PostgreSQL 12+
- Redis 6+
- Maven 3.6+
- TMDb API key

## 🚀 Quick Start

### 1. Clone Repository
```bash
git clone https://github.com/yourusername/movie-recommender.git
cd movie-recommender
```

### 2. Database Setup
```sql
-- Create PostgreSQL database
CREATE DATABASE movierecommender;
CREATE USER movieuser WITH PASSWORD 'yourpassword';
GRANT ALL PRIVILEGES ON DATABASE movierecommender TO movieuser;
```

### 3. Backend Configuration
```bash
cd backend
```

Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/movierecommender
spring.datasource.username=movieuser
spring.datasource.password=yourpassword
tmdb.api.key=your_tmdb_api_key
app.jwt.secret=your_jwt_secret_key
```

### 4. Start Backend
```bash
mvn clean install
mvn spring-boot:run
```

### 5. Python ML Service Setup
```bash
cd ../python-ml-service
pip install -r requirements.txt
```

Update database connection in `database.py`:
```python
DATABASE_URL = "postgresql://movieuser:yourpassword@localhost:5432/movierecommender"
```

### 6. Start ML Service
```bash
python app.py
```

## 🔧 Configuration

### Environment Variables

#### Backend
```bash
export DATABASE_URL=postgresql://localhost:5432/movierecommender
export REDIS_URL=redis://localhost:6379
export TMDB_API_KEY=your_tmdb_api_key
export JWT_SECRET=your_jwt_secret
export ML_SERVICE_URL=http://localhost:5000
```

#### Python ML Service
```bash
export DATABASE_URL=postgresql://localhost:5432/movierecommender
export FLASK_ENV=production  # for production
export PORT=5000
```

## 📚 API Documentation

### Authentication Endpoints
```
POST /api/auth/signin      - User login
POST /api/auth/signup      - User registration
```

### Movie Endpoints
```
GET  /api/movies                    - Get all movies (paginated)
GET  /api/movies/{id}              - Get movie by ID
GET  /api/movies/search            - Search movies
GET  /api/movies/popular           - Get popular movies
GET  /api/movies/genres/{genre}    - Get movies by genre
```

### Recommendation Endpoints
```
POST /api/recommendations/generate     - Generate recommendations
POST /api/recommendations/cold-start   - Cold start recommendations
GET  /api/recommendations             - Get user recommendations
GET  /api/recommendations/paged       - Get paginated recommendations
```

### User Endpoints
```
GET  /api/users/profile              - Get user profile
PUT  /api/users/profile              - Update user profile
POST /api/users/letterboxd/sync      - Sync Letterboxd data
GET  /api/users/ratings              - Get user ratings
POST /api/users/ratings              - Add rating
```

### Admin Endpoints
```
GET  /api/admin/dashboard            - Admin dashboard
POST /api/admin/dataset/rebuild      - Rebuild dataset
POST /api/admin/ml/retrain          - Retrain ML models
GET  /api/admin/system/metrics       - System metrics
```

## 🤖 Machine Learning Pipeline

### 1. Data Collection
- Letterboxd profile scraping
- TMDb metadata enrichment
- User rating aggregation

### 2. Dataset Preparation
- Data cleaning and validation
- User-item matrix creation
- Train/validation/test splits

### 3. Model Training
- **SVD (Singular Value Decomposition)**
- **NMF (Non-negative Matrix Factorization)**
- **Content-Based Filtering**
- **Hybrid Model Combination**

### 4. Recommendation Generation
- User-based collaborative filtering
- Item-based collaborative filtering
- Content similarity matching
- Hybrid score calculation

## 🚀 Deployment

### Heroku Deployment

#### Backend
```bash
cd backend
heroku create your-app-backend
heroku addons:create heroku-postgresql:hobby-dev
heroku addons:create heroku-redis:hobby-dev
heroku config:set TMDB_API_KEY=your_key
heroku config:set JWT_SECRET=your_secret
git push heroku main
```

#### Python ML Service
```bash
cd python-ml-service
heroku create your-app-ml-service
heroku config:set DATABASE_URL=your_postgres_url
git push heroku main
```

### Docker Deployment
```bash
# Backend
cd backend
docker build -t movie-recommender-backend .
docker run -p 8080:8080 movie-recommender-backend

# ML Service
cd python-ml-service
docker build -t movie-recommender-ml .
docker run -p 5000:5000 movie-recommender-ml
```

## 📊 Performance & Monitoring

### Metrics Available
- Dataset health and statistics
- Model accuracy and performance
- User engagement metrics
- System resource usage
- API response times

### Monitoring Endpoints
```
GET /actuator/health     - Application health
GET /actuator/metrics    - Application metrics
GET /api/admin/dashboard - Admin dashboard
```

## 🧪 Testing

### Backend Tests
```bash
cd backend
mvn test
```

### ML Service Tests
```bash
cd python-ml-service
python -m pytest tests/
```

## 🔒 Security

- JWT-based authentication
- Role-based authorization (USER, ADMIN)
- Password encryption with BCrypt
- CORS configuration
- Input validation and sanitization
- Rate limiting for external API calls

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- Create an [Issue](https://github.com/yourusername/movie-recommender/issues)
- Contact: your.email@example.com

## 🙏 Acknowledgments

- [TMDb](https://www.themoviedb.org/) for movie metadata
- [Letterboxd](https://letterboxd.com/) for inspiration
- Spring Boot and scikit-learn communities