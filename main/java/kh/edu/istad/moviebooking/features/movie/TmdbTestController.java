package kh.edu.istad.moviebooking.features.movie;

import kh.edu.istad.moviebooking.intergration.tmdb.TmdbClient;
import kh.edu.istad.moviebooking.intergration.tmdb.dto.TmdbSearchResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/tmdb")
public class TmdbTestController {

    private final TmdbClient tmdbClient;

    public TmdbTestController(TmdbClient tmdbClient) {
        this.tmdbClient = tmdbClient;
    }

    @GetMapping
    public TmdbSearchResponse search(
            @RequestParam String query
    ) {
        return tmdbClient.searchMovies(query);
    }
}
