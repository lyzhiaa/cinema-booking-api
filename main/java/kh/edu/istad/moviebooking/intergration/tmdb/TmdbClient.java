package kh.edu.istad.moviebooking.intergration.tmdb;

import kh.edu.istad.moviebooking.intergration.tmdb.dto.TmdbSearchResponse;
import kh.edu.istad.moviebooking.intergration.tmdb.dto.search.TmdbMovieDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class TmdbClient {

    private final RestClient tmdbRestClient;

//    search
    public TmdbSearchResponse searchMovies(String query) {

        return tmdbRestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/movie")
                        .queryParam("query", query)
                        .queryParam("include_adult", false)
                        .queryParam("language", "en-US")
                        .build()
                )
                .retrieve()
                .body(TmdbSearchResponse.class);
    }
    // get movie details
    public TmdbMovieDetailResponse getMovieDetails(Long tmdbId) {

        return tmdbRestClient
                .get()
                .uri("/movie/{id}", tmdbId)
                .retrieve()
                .body(TmdbMovieDetailResponse.class);
    }
}