package com.movierecommender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class LetterboxdScraperConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    @ConfigurationProperties(prefix = "letterboxd.scraper")
    public LetterboxdScraperProperties letterboxdScraperProperties() {
        return new LetterboxdScraperProperties();
    }
    
    public static class LetterboxdScraperProperties {
        private String baseUrl = "http://localhost:5000";
        private int timeout = 30000;
        private boolean enabled = true;
        
        // Getters and setters
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
        
        public int getTimeout() {
            return timeout;
        }
        
        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
