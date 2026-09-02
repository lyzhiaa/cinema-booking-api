package kh.edu.istad.moviebooking.features.movie;

import kh.edu.istad.moviebooking.features.movie.dto.MovieResponse;

import java.util.List;

public interface MovieService {
//    get all movie
    List<MovieResponse> getAllMovies();
//    import movie from tmdb
    MovieResponse importMovie(Long tmdbId);
}
