import React, { useState } from 'react';
import LetterboxdIntegrationSimple from '../components/LetterboxdIntegrationSimple';
import { LetterboxdScrapeResponse } from '../services/letterboxdApi';

const LetterboxdPage: React.FC = () => {
  const [scrapedData, setScrapedData] = useState<LetterboxdScrapeResponse | null>(null);

  const handleDataScraped = (data: LetterboxdScrapeResponse) => {
    setScrapedData(data);
    // Here you could save the data to your backend or perform other actions
    console.log('Scraped data received:', data);
    
    // Example: You could send this data to your movie/user API to store it
    // saveScrapeDataToBackend(data);
  };

  return (
    <div className="letterboxd-page">
      <div className="page-header">
        <h1>Letterboxd Integration</h1>
        <p>
          Connect your Letterboxd account to import your movie ratings and watchlist. 
          This will help us provide better movie recommendations based on your preferences.
        </p>
      </div>
      
      <LetterboxdIntegrationSimple onDataScraped={handleDataScraped} />
      
      {scrapedData && scrapedData.success && (
        <div className="scraped-data-actions">
          <h2>What's Next?</h2>
          <div className="action-cards">
            <div className="action-card">
              <h3>📊 View Analytics</h3>
              <p>Analyze your movie watching patterns and preferences</p>
              <button className="action-button">View Stats</button>
            </div>
            
            <div className="action-card">
              <h3>🎬 Get Recommendations</h3>
              <p>Get personalized movie recommendations based on your ratings</p>
              <button className="action-button">Get Recommendations</button>
            </div>
            
            <div className="action-card">
              <h3>📚 Manage Data</h3>
              <p>Review and manage your imported movie data</p>
              <button className="action-button">Manage Movies</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default LetterboxdPage;
