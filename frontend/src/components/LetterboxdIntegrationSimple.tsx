import React, { useState, useEffect } from 'react';
import { useLetterboxd } from '../hooks/useLetterboxd';
import { ScrapeOptions, LetterboxdRating, LetterboxdWatchlistFilm } from '../types';
import './LetterboxdIntegration.css';

interface LetterboxdIntegrationSimpleProps {
  onDataScraped?: (data: any) => void;
}

const LetterboxdIntegrationSimple: React.FC<LetterboxdIntegrationSimpleProps> = ({ onDataScraped }) => {
  const [username, setUsername] = useState<string>('');
  const [scrapeOptions, setScrapeOptions] = useState<ScrapeOptions>({
    includeRatings: true,
    includeWatchlist: true,
    ratingLimit: 100
  });
  const [validationResult, setValidationResult] = useState<{
    isValid: boolean;
    message: string;
  } | null>(null);

  const {
    isLoading,
    error,
    serviceHealth,
    profile,
    scrapeResult,
    validateUser,
    scrapeUserData,
    quickScrape,
    checkServiceHealth,
    clearError,
    clearResults
  } = useLetterboxd();

  useEffect(() => {
    checkServiceHealth();
  }, [checkServiceHealth]);

  useEffect(() => {
    if (scrapeResult && onDataScraped) {
      onDataScraped(scrapeResult);
    }
  }, [scrapeResult, onDataScraped]);

  const handleValidateUser = async () => {
    if (!username.trim()) {
      return;
    }    clearError();
    const result = await validateUser(username.trim());
    setValidationResult({
      isValid: result.exists,
      message: result.message
    });
  };

  const handleScrapeData = async (isQuick: boolean = false) => {
    if (!username.trim()) {
      return;
    }

    clearError();
    if (isQuick) {
      await quickScrape(username.trim());
    } else {
      await scrapeUserData(username.trim(), scrapeOptions);
    }
  };

  const handleUsernameChange = (value: string) => {
    setUsername(value);
    if (validationResult) {
      setValidationResult(null);
    }
    if (error) {
      clearError();
    }
  };

  const handleOptionsChange = (option: keyof ScrapeOptions, value: boolean | number) => {
    setScrapeOptions(prev => ({
      ...prev,
      [option]: value
    }));
  };

  return (
    <div className="letterboxd-integration">
      <div className="letterboxd-header">
        <h2>Letterboxd Integration</h2>
        <div className="service-status">
          <span className={`status-indicator ${serviceHealth ? 'healthy' : 'unhealthy'}`}>
            {serviceHealth ? '🟢' : '🔴'}
          </span>
          <span>Service: {serviceHealth ? 'Available' : 'Unavailable'}</span>
        </div>
      </div>

      {!serviceHealth && (
        <div className="service-warning">
          ⚠️ Letterboxd scraper service is currently unavailable. Please check the service status.
        </div>
      )}

      <div className="username-section">
        <div className="input-group">
          <label htmlFor="username">Letterboxd Username:</label>
          <input
            id="username"
            type="text"
            value={username}
            onChange={(e) => handleUsernameChange(e.target.value)}
            placeholder="Enter Letterboxd username"
            disabled={!serviceHealth || isLoading}
          />
          <button
            onClick={handleValidateUser}
            disabled={isLoading || !serviceHealth || !username.trim()}
            className="validate-button"
          >
            {isLoading ? 'Validating...' : 'Validate User'}
          </button>
        </div>

        {error && <div className="error-message">{error}</div>}

        {validationResult && (
          <div className={`validation-result ${validationResult.isValid ? 'valid' : 'invalid'}`}>
            {validationResult.isValid ? '✅' : '❌'} {validationResult.message}
          </div>
        )}
      </div>

      {profile && (
        <div className="profile-section">
          <h3>Profile Information</h3>
          <div className="profile-info">
            <div className="profile-item">
              <strong>Username:</strong> {profile.username}
            </div>
            {profile.displayName && (
              <div className="profile-item">
                <strong>Display Name:</strong> {profile.displayName}
              </div>
            )}
            {profile.filmsWatched !== undefined && (
              <div className="profile-item">
                <strong>Films Watched:</strong> {profile.filmsWatched}
              </div>
            )}
            {profile.followers !== undefined && (
              <div className="profile-item">
                <strong>Followers:</strong> {profile.followers}
              </div>
            )}
            {profile.following !== undefined && (
              <div className="profile-item">
                <strong>Following:</strong> {profile.following}
              </div>
            )}
            {profile.bio && (
              <div className="profile-item">
                <strong>Bio:</strong> {profile.bio}
              </div>
            )}
            {profile.location && (
              <div className="profile-item">
                <strong>Location:</strong> {profile.location}
              </div>
            )}
            {profile.website && (
              <div className="profile-item">
                <strong>Website:</strong> {profile.website}
              </div>
            )}
            {profile.joinedDate && (
              <div className="profile-item">
                <strong>Joined:</strong> {profile.joinedDate}
              </div>
            )}
          </div>
        </div>
      )}

      {validationResult?.isValid && (
        <div className="scrape-section">
          <h3>Scrape Options</h3>
          
          <div className="scrape-options">
            <div className="option-group">
              <label>
                <input
                  type="checkbox"
                  checked={scrapeOptions.includeRatings}
                  onChange={(e) => handleOptionsChange('includeRatings', e.target.checked)}
                />
                Include Ratings
              </label>
            </div>
            
            <div className="option-group">
              <label>
                <input
                  type="checkbox"
                  checked={scrapeOptions.includeWatchlist}
                  onChange={(e) => handleOptionsChange('includeWatchlist', e.target.checked)}
                />
                Include Watchlist
              </label>
            </div>
            
            <div className="option-group">
              <label>
                Rating Limit:
                <input
                  type="number"
                  min="1"
                  max="1000"
                  value={scrapeOptions.ratingLimit}
                  onChange={(e) => handleOptionsChange('ratingLimit', parseInt(e.target.value) || 100)}
                />
              </label>
            </div>
          </div>

          <div className="scrape-buttons">
            <button
              onClick={() => handleScrapeData(true)}
              disabled={isLoading || !serviceHealth}
              className="quick-scrape-button"
            >
              {isLoading ? 'Scraping...' : 'Quick Scrape (50 items)'}
            </button>
            
            <button
              onClick={() => handleScrapeData(false)}
              disabled={isLoading || !serviceHealth}
              className="full-scrape-button"
            >
              {isLoading ? 'Scraping...' : `Full Scrape (${scrapeOptions.ratingLimit} items)`}
            </button>
          </div>
        </div>
      )}

      {scrapeResult && (
        <div className="scrape-results">
          <h3>Scrape Results</h3>
          
          {scrapeResult.success ? (
            <div className="results-summary">
              <div className="result-item">
                ✅ <strong>Success!</strong>
              </div>              <div className="result-item">
                <strong>Ratings scraped:</strong> {scrapeResult.totalRatings}
              </div>
              <div className="result-item">
                <strong>Watchlist items:</strong> {scrapeResult.totalWatchlistItems}
              </div>
              {scrapeResult.scrapedAt && (
                <div className="result-item">
                  <strong>Scraped at:</strong> {new Date(scrapeResult.scrapedAt).toLocaleString()}
                </div>
              )}
              
              {scrapeResult.ratings && scrapeResult.ratings.length > 0 && (
                <div className="data-preview">
                  <h4>Sample Ratings ({scrapeResult.ratings.length} total):</h4>
                  <div className="movie-list">
                    {scrapeResult.ratings.slice(0, 5).map((rating: LetterboxdRating, index: number) => (
                      <div key={index} className="movie-item">
                        <strong>{rating.filmTitle}</strong> {rating.filmYear && `(${rating.filmYear})`}
                        {rating.rating && ` - ⭐ ${rating.rating}/5`}
                        {rating.watchedDate && <div className="watched-date">Watched: {rating.watchedDate}</div>}
                      </div>
                    ))}
                    {scrapeResult.ratings.length > 5 && (
                      <div className="more-items">
                        ... and {scrapeResult.ratings.length - 5} more
                      </div>
                    )}
                  </div>
                </div>
              )}

              {scrapeResult.watchlist && scrapeResult.watchlist.length > 0 && (
                <div className="data-preview">
                  <h4>Sample Watchlist ({scrapeResult.watchlist.length} total):</h4>
                  <div className="movie-list">
                    {scrapeResult.watchlist.slice(0, 5).map((film: LetterboxdWatchlistFilm, index: number) => (
                      <div key={index} className="movie-item">
                        <strong>{film.filmTitle}</strong> {film.filmYear && `(${film.filmYear})`}
                        {film.directors && film.directors.length > 0 && (
                          <div className="directors">Director(s): {film.directors.join(', ')}</div>
                        )}
                        {film.genres && film.genres.length > 0 && (
                          <div className="genres">Genre(s): {film.genres.join(', ')}</div>
                        )}
                      </div>
                    ))}
                    {scrapeResult.watchlist.length > 5 && (
                      <div className="more-items">
                        ... and {scrapeResult.watchlist.length - 5} more
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>
          ) : (            <div className="results-error">
              ❌ <strong>Scraping failed:</strong> {scrapeResult.errorMessage}
            </div>
          )}

          <div className="results-actions">
            <button 
              onClick={clearResults}
              className="clear-results-button"
            >
              Clear Results
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default LetterboxdIntegrationSimple;
