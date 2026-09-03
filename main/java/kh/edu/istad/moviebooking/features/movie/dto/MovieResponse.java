package kh.edu.istad.moviebooking.features.movie.dto;

import kh.edu.istad.moviebooking.domain.enums.MovieStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record MovieResponse(
        UUID uuid,
        Long tmdbId,
        String title,
        String originalTitle,
        String overview,
        String posterUrl,
        String backdropUrl,
        Integer runtimeMinutes,
        LocalDate releaseDate,
        String ageRating,
        String language,
        MovieStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
