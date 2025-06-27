import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import MoviesPage from './pages/MoviesPage';
import UsersPage from './pages/UsersPage';
import './App.css';

const App: React.FC = () => {
  return (
    <Router>
      <div className="app">
        <header className="app-header">
          <div className="container">
            <Link to="/" className="logo">
              🎬 Movie Recommender
            </Link>
            <nav className="nav">
              <Link to="/movies" className="nav-link">Movies</Link>
              <Link to="/users" className="nav-link">Users</Link>
            </nav>
          </div>
        </header>

        <main className="main-content">
          <div className="container">
            <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/movies" element={<MoviesPage />} />
              <Route path="/users" element={<UsersPage />} />
            </Routes>
          </div>
        </main>
      </div>
    </Router>
  );
};

const HomePage: React.FC = () => {
  return (
    <div className="home-page">
      <h1>Welcome to Movie Recommender</h1>
      <p>Manage your movies and users with this simple application.</p>
      <div className="home-actions">
        <Link to="/movies" className="btn btn-primary">
          View Movies
        </Link>
        <Link to="/users" className="btn btn-secondary">
          View Users
        </Link>
      </div>
    </div>
  );
};

export default App;
