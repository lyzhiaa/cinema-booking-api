package kh.edu.istad.moviebooking.features.movie;

import jakarta.validation.Valid;
import kh.edu.istad.moviebooking.features.common.PageResponse;
import kh.edu.istad.moviebooking.features.movie.dto.MovieResponse;
import kh.edu.istad.moviebooking.features.movie.dto.UpdateMovieStatusRequest;
import kh.edu.istad.moviebooking.intergration.tmdb.TmdbClient;
import kh.edu.istad.moviebooking.intergration.tmdb.dto.TmdbSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/movies")
public class MovieController {
    private final MovieService movieService;
    private final TmdbClient tmdbClient;

    //    get all movies
    @GetMapping
    public PageResponse<MovieResponse> getAllMovies(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "createdAt")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String direction

    ) {

        return movieService.getAllMovies(page, size, sortBy, direction);
    }
//    get movie by uuid
    @GetMapping("/{uuid}")
    MovieResponse getMovieByUuid(@Valid @PathVariable("uuid") UUID uuid){
        return movieService.getMovieByUuid(uuid);
    }
//    import movie from TMDB
    @PostMapping("/import/{tmdbId}")
    public MovieResponse importMovie(@PathVariable Long tmdbId) {
        return movieService.importMovie(tmdbId);
    }
//    update movie status
    @PatchMapping("/{uuid}/status")
    public MovieResponse updateMovieStatus(@PathVariable UUID uuid, @RequestBody UpdateMovieStatusRequest updateMovieStatusRequest){
        return movieService.updateMovieStatus(uuid, updateMovieStatusRequest);
    }
//    search movies
    @GetMapping("/search")
    public TmdbSearchResponse search(
            @RequestParam String query
    ) {
        return tmdbClient.searchMovies(query);
    }
//    delete movie
    @DeleteMapping("/{uuid}")
    void deleteMovieByUuid(@PathVariable("uuid") UUID uuid) {
        movieService.deleteMovieByUuid(uuid);
    }


}
