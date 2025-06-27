# Movie Recommender Frontend

A React TypeScript frontend for the Movie Recommender system with Letterboxd integration and machine learning-powered recommendations.

## Features

- **User Authentication**: Login/Register with JWT authentication
- **Letterboxd Integration**: Sync movie ratings and watchlist from Letterboxd
- **Movie Browsing**: Search, filter, and browse movies with advanced filtering
- **Personalized Recommendations**: ML-powered movie recommendations with multiple algorithms
- **User Dashboard**: Profile management and viewing statistics
- **Responsive Design**: Mobile-first design with Material-UI components
- **Real-time Sync Status**: Live updates on Letterboxd data synchronization

## Tech Stack

- **React 18** with TypeScript
- **Material-UI (MUI)** for UI components
- **Redux Toolkit** for state management
- **Redux Persist** for state persistence
- **React Query** for server state management
- **React Router** for navigation
- **Axios** for API communication

## Project Structure

```
src/
├── components/          # Reusable UI components
│   ├── LoadingButton.tsx
│   ├── MovieCard.tsx
│   ├── Navbar.tsx
│   └── ProtectedRoute.tsx
├── hooks/               # Custom React hooks
│   └── redux.ts
├── pages/               # Page components
│   ├── Home.tsx
│   ├── Login.tsx
│   ├── Register.tsx
│   ├── Movies.tsx
│   ├── Recommendations.tsx
│   └── Profile.tsx
├── services/            # API service layer
│   ├── api.ts
│   ├── movieApi.ts
│   ├── recommendationApi.ts
│   └── userApi.ts
├── store/               # Redux store configuration
│   ├── index.ts
│   ├── authSlice.ts
│   ├── movieSlice.ts
│   └── recommendationSlice.ts
├── types/               # TypeScript type definitions
│   └── index.ts
├── App.tsx              # Main App component
└── index.tsx            # Application entry point
```

## Getting Started

### Prerequisites

- Node.js 16+ and npm
- Backend Spring Boot application running on port 8080
- Python ML service running

### Installation

1. Install dependencies:
```bash
npm install
```

2. Copy environment variables:
```bash
cp .env.example .env
```

3. Configure environment variables in `.env`:
```
REACT_APP_API_URL=http://localhost:8080/api
```

### Development

Start the development server:
```bash
npm start
```

The app will be available at `http://localhost:3000`

### Building for Production

Build the application:
```bash
npm run build
```

### Testing

Run tests:
```bash
npm test
```

## Key Features Explained

### Authentication Flow
- User registration and login with form validation
- JWT token management with automatic refresh
- Protected routes requiring authentication
- Persistent login state across browser sessions

### Letterboxd Integration
- Input Letterboxd username to sync movie data
- Real-time sync status updates with progress indicators
- Import ratings, reviews, and watchlist data
- Background sync with status polling

### Movie Discovery
- Advanced search with filters (genre, year, rating)
- Pagination for large result sets
- Movie cards with poster images and metadata
- Popular movies showcase on homepage

### Personalized Recommendations
- Multiple ML algorithms (collaborative, content-based, hybrid)
- Recommendation confidence scores
- Algorithm-specific explanations
- User interaction tracking (viewed, clicked, hidden)
- Recommendation refresh functionality

### User Dashboard
- Profile information and account management
- Movie statistics and favorite genres
- Recent activity timeline
- Letterboxd sync history and status

## API Integration

The frontend communicates with the Spring Boot backend through RESTful APIs:

- **Auth API**: `/api/auth/*` - Authentication endpoints
- **User API**: `/api/users/*` - User profile and ratings
- **Movie API**: `/api/movies/*` - Movie data and search
- **Recommendation API**: `/api/recommendations/*` - ML recommendations

## State Management

### Redux Store Structure
- **auth**: User authentication state and profile
- **movies**: Movie data, search results, and filters
- **recommendations**: Recommendation data and interactions

### Persistence
- Authentication state persisted to localStorage
- Popular movies and genres cached for performance
- User preferences maintained across sessions

## Component Architecture

### Reusable Components
- **MovieCard**: Consistent movie display across pages
- **LoadingButton**: Button with loading states
- **ProtectedRoute**: Authentication guard for routes

### Page Components
- **Home**: Landing page with popular movies
- **Movies**: Movie browsing with search and filters
- **Recommendations**: Personalized recommendation display
- **Profile**: User dashboard and Letterboxd integration

## Error Handling

- Axios interceptors for global error handling
- User-friendly error messages with Material-UI alerts
- Automatic token refresh and login redirect
- Loading states for all async operations

## Performance Optimizations

- React Query for server state caching
- Redux Persist for offline state
- Lazy loading for large movie lists
- Optimized re-renders with proper memoization

## Deployment

### Development
```bash
npm start
```

### Production Build
```bash
npm run build
npm install -g serve
serve -s build -l 3000
```

### Environment Variables
```
REACT_APP_API_URL=https://your-api-domain.com/api
REACT_APP_ENABLE_DEV_TOOLS=false
```

## Contributing

1. Follow TypeScript best practices
2. Use Material-UI components consistently
3. Implement proper error handling
4. Add loading states for async operations
5. Write comprehensive JSDoc comments
6. Follow the established project structure

## Troubleshooting

### Common Issues

1. **API Connection Errors**
   - Verify backend is running on correct port
   - Check CORS configuration in Spring Boot
   - Confirm API_URL in environment variables

2. **Authentication Issues**
   - Clear localStorage and try logging in again
   - Check JWT token expiration
   - Verify backend JWT configuration

3. **Letterboxd Sync Failures**
   - Ensure Python ML service is running
   - Check Letterboxd username spelling
   - Verify network connectivity

### Development Tips

- Use Redux DevTools for debugging state changes
- Enable React Query DevTools in development
- Check browser network tab for API call details
- Use Material-UI theme debugging tools