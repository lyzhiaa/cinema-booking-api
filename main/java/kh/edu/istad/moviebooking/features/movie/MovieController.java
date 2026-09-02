package kh.edu.istad.moviebooking.features.movie;

import kh.edu.istad.moviebooking.features.movie.dto.MovieResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/movies")
public class MovieController {
    private final MovieService movieService;

//    get all movies
    @GetMapping
    public List<MovieResponse> getAllMovies() {
        return movieService.getAllMovies();
    }
//    import movie from TMDB
    @PostMapping("/import/{tmdbId}")
    public MovieResponse importMovie(@PathVariable Long tmdbId) {
        return movieService.importMovie(tmdbId);
    }

}
