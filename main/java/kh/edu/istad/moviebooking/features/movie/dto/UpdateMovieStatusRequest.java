package kh.edu.istad.moviebooking.features.movie.dto;

import kh.edu.istad.moviebooking.domain.enums.MovieStatus;

public record UpdateMovieStatusRequest(
        MovieStatus status
) {
}
