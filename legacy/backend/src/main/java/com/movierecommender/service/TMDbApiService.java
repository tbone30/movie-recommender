package com.movierecommender.service;

import com.movierecommender.entity.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TMDbApiService {

    private static final Logger logger = LoggerFactory.getLogger(TMDbApiService.class);

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.base.url}")
    private String baseUrl;

    private final WebClient webClient;

    public TMDbApiService() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

    public Movie enrichMovieData(Movie movie) {
        try {
            if (movie.getTmdbId() == null) {
                // Search for movie by title and year
                Long tmdbId = searchMovieByTitleAndYear(movie.getTitle(), movie.getYear());
                if (tmdbId != null) {
                    movie.setTmdbId(tmdbId);
                } else {
                    logger.warn("Could not find TMDb ID for movie: {} ({})", movie.getTitle(), movie.getYear());
                    return movie;
                }
            }

            // Get detailed movie information
            Map<String, Object> movieDetails = getMovieDetails(movie.getTmdbId());
            if (movieDetails != null) {
                enrichMovieFromTMDbData(movie, movieDetails);
            }

            return movie;
        } catch (Exception e) {
            logger.error("Error enriching movie data for movie ID {}: {}", movie.getTmdbId(), e.getMessage());
            return movie;
        }
    }

    private Long searchMovieByTitleAndYear(String title, Integer year) {
        try {
            String url = String.format("%s/search/movie?api_key=%s&query=%s&year=%d", 
                    baseUrl, apiKey, title, year);

            Map<String, Object> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response != null && response.get("results") instanceof List) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                if (!results.isEmpty()) {
                    Map<String, Object> firstResult = results.get(0);
                    return ((Number) firstResult.get("id")).longValue();
                }
            }
        } catch (WebClientResponseException e) {
            logger.error("TMDb API error searching for movie {}: {}", title, e.getMessage());
        } catch (Exception e) {
            logger.error("Error searching for movie {}: {}", title, e.getMessage());
        }
        return null;
    }

    private Map<String, Object> getMovieDetails(Long tmdbId) {
        try {
            String url = String.format("%s/movie/%d?api_key=%s&append_to_response=credits", 
                    baseUrl, tmdbId, apiKey);

            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
        } catch (WebClientResponseException e) {
            logger.error("TMDb API error getting movie details for ID {}: {}", tmdbId, e.getMessage());
        } catch (Exception e) {
            logger.error("Error getting movie details for ID {}: {}", tmdbId, e.getMessage());
        }
        return null;
    }

    private void enrichMovieFromTMDbData(Movie movie, Map<String, Object> tmdbData) {
        // Basic information
        if (tmdbData.get("overview") != null) {
            movie.setOverview((String) tmdbData.get("overview"));
        }

        if (tmdbData.get("runtime") != null) {
            movie.setRuntime(((Number) tmdbData.get("runtime")).intValue());
        }

        if (tmdbData.get("budget") != null) {
            movie.setBudget(((Number) tmdbData.get("budget")).longValue());
        }

        if (tmdbData.get("revenue") != null) {
            movie.setRevenue(((Number) tmdbData.get("revenue")).longValue());
        }

        // Poster URL
        if (tmdbData.get("poster_path") != null) {
            String posterPath = (String) tmdbData.get("poster_path");
            movie.setPosterUrl("https://image.tmdb.org/t/p/w500" + posterPath);
        }

        // Genres
        if (tmdbData.get("genres") instanceof List) {
            List<Map<String, Object>> genres = (List<Map<String, Object>>) tmdbData.get("genres");
            Set<String> genreNames = new HashSet<>();
            for (Map<String, Object> genre : genres) {
                genreNames.add((String) genre.get("name"));
            }
            movie.setGenres(genreNames);
        }

        // Production countries
        if (tmdbData.get("production_countries") instanceof List) {
            List<Map<String, Object>> countries = (List<Map<String, Object>>) tmdbData.get("production_countries");
            Set<String> countryNames = new HashSet<>();
            for (Map<String, Object> country : countries) {
                countryNames.add((String) country.get("name"));
            }
            movie.setCountries(countryNames);
        }

        // Spoken languages
        if (tmdbData.get("spoken_languages") instanceof List) {
            List<Map<String, Object>> languages = (List<Map<String, Object>>) tmdbData.get("spoken_languages");
            Set<String> languageNames = new HashSet<>();
            for (Map<String, Object> language : languages) {
                languageNames.add((String) language.get("english_name"));
            }
            movie.setLanguages(languageNames);
        }

        // Credits (director and actors)
        if (tmdbData.get("credits") instanceof Map) {
            Map<String, Object> credits = (Map<String, Object>) tmdbData.get("credits");
            
            // Director
            if (credits.get("crew") instanceof List) {
                List<Map<String, Object>> crew = (List<Map<String, Object>>) credits.get("crew");
                for (Map<String, Object> member : crew) {
                    if ("Director".equals(member.get("job"))) {
                        movie.setDirector((String) member.get("name"));
                        break;
                    }
                }
            }

            // Main actors (top 5)
            if (credits.get("cast") instanceof List) {
                List<Map<String, Object>> cast = (List<Map<String, Object>>) credits.get("cast");
                Set<String> actorNames = new HashSet<>();
                int count = 0;
                for (Map<String, Object> actor : cast) {
                    if (count >= 5) break;
                    actorNames.add((String) actor.get("name"));
                    count++;
                }
                movie.setActors(actorNames);
            }
        }
    }
}