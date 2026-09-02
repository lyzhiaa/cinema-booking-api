package kh.edu.istad.moviebooking.intergration.tmdb.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbMovieDetailResponse(
        Long id,

        String title,

        @JsonProperty("original_title")
        String originalTitle,

        String overview,

        @JsonProperty("poster_path")
        String posterPath,

        @JsonProperty("backdrop_path")
        String backdropPath,

        Integer runtime,

        @JsonProperty("release_date")
        String releaseDate,

        @JsonProperty("original_language")
        String originalLanguage
) {
}
