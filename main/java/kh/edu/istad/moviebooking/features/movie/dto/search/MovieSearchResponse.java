package kh.edu.istad.moviebooking.features.movie.dto.search;

public record MovieSearchResponse(
        Long tmdbId,
        String title,
        String originalTitle,
        String overview,
        String posterPath,
        String backdropPath,
        String releaseDate,
        String language
) {
}
