package kh.edu.istad.moviebooking.features.movie;

import kh.edu.istad.moviebooking.domain.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MovieRepository extends JpaRepository<Movie, Long> {
//    find movie by TMDBid
    Boolean existsByTmdbId(Long tmdbId);
//    find movie by uuid
    Optional<Movie> findByUuid(UUID uuid);

    Optional<Object> findMovieByUuid(UUID uuid);
}
