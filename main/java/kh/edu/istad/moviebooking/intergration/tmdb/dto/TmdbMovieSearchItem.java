package kh.edu.istad.moviebooking.intergration.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbMovieSearchItem(

        Long id,

        String title,

        @JsonProperty("original_title")
        String originalTitle,

        String overview,

        @JsonProperty("poster_path")
        String posterPath,

        @JsonProperty("backdrop_path")
        String backdropPath,

        @JsonProperty("release_date")
        String releaseDate,

        @JsonProperty("original_language")
        String language

) {
}
