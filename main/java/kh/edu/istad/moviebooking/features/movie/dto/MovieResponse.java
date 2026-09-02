package kh.edu.istad.moviebooking.features.movie.dto;

import kh.edu.istad.moviebooking.domain.enums.MovieStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MovieResponse(
        Long id,
        Long tmdbId,
        String title,
        String originalTitle,
        String overview,
        String posterPath,
        String backdropPath,
        Integer runtimeMinutes,
        LocalDate releaseDate,
        String ageRating,
        String language,
        MovieStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
