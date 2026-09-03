package kh.edu.istad.moviebooking.features.movie;

import kh.edu.istad.moviebooking.features.movie.dto.MovieResponse;
import kh.edu.istad.moviebooking.features.movie.dto.UpdateMovieStatusRequest;

import java.util.List;
import java.util.UUID;

public interface MovieService {
//    get all movie
    List<MovieResponse> getAllMovies();
//    get movie by uuid
    MovieResponse getMovieByUuid(UUID uuid);
//    import movie from tmdb
    MovieResponse importMovie(Long tmdbId);
//    update the status of movie
    MovieResponse updateMovieStatus(UUID uuid, UpdateMovieStatusRequest updateMovieStatusRequest);
//    delete movie
    void deleteMovieByUuid(UUID uuid);
}
