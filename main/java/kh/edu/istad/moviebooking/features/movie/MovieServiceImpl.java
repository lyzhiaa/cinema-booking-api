package kh.edu.istad.moviebooking.features.movie;

import jakarta.transaction.Transactional;
import kh.edu.istad.moviebooking.domain.Movie;
import kh.edu.istad.moviebooking.domain.enums.MovieStatus;
import kh.edu.istad.moviebooking.features.movie.dto.MovieResponse;
import kh.edu.istad.moviebooking.features.movie.dto.UpdateMovieStatusRequest;
import kh.edu.istad.moviebooking.intergration.tmdb.TmdbClient;
import kh.edu.istad.moviebooking.intergration.tmdb.dto.search.TmdbMovieDetailResponse;
import kh.edu.istad.moviebooking.mapper.MovieMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {
    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;
    private final TmdbClient tmdbClient;

    //    get all movie
    @Override
    public List<MovieResponse> getAllMovies() {
        List<Movie> movies = movieRepository.findAll();

        return movieMapper.toMovieResponseList(movies);
    }

//    get movie by uuid
    @Override
    public MovieResponse getMovieByUuid(UUID uuid) {
        // find movie
        Movie movie = movieRepository.findByUuid(uuid)
                .orElseThrow(()-> new RuntimeException("Movie not found."));
        return movieMapper.toMovieResponse(movie);
    }

    //    import movie from tmdb
    @Override
    @Transactional
    public MovieResponse importMovie(Long tmdbId) {
//        validate if the movie is already exist
        if (movieRepository.existsByTmdbId(tmdbId)) {
            throw new RuntimeException(
                    "Movie already imported"
            );
        }

        TmdbMovieDetailResponse tmdbMovie =
                tmdbClient.getMovieDetails(tmdbId);

        if (tmdbMovie == null) {
            throw new RuntimeException(
                    "Unable to retrieve movie from TMDB"
            );
        }

        Movie movie = movieMapper.fromTmdbDetail(tmdbMovie);

        movie.setStatus(MovieStatus.ACTIVE);

        Movie savedMovie = movieRepository.save(movie);

        return movieMapper.toMovieResponse(savedMovie);
    }

//    update status of the movie
    @Override
    public MovieResponse updateMovieStatus(UUID uuid, UpdateMovieStatusRequest updateMovieStatusRequest) {
//        find movie
        Movie movie = movieRepository.findByUuid(uuid)
                .orElseThrow(()-> new RuntimeException("Movie not found."));
        movie.setStatus(updateMovieStatusRequest.status());

        Movie updatedMovie = movieRepository.save(movie);

        return movieMapper.toMovieResponse(updatedMovie);
    }
}
