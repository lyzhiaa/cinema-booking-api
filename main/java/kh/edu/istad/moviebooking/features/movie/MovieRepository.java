package kh.edu.istad.moviebooking.features.movie;

import kh.edu.istad.moviebooking.domain.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
//    find movie by id
    Boolean existsByTmdbId(Long tmdbId);
}
