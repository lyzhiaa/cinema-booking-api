package kh.edu.istad.moviebooking.intergration.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TmdbSearchResponse(

        Integer page,

        List<TmdbMovieSearchItem> results,

        @JsonProperty("total_pages")
        Integer totalPages,

        @JsonProperty("total_results")
        Integer totalResults

) {
}
