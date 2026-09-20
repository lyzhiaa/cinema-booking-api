package kh.edu.istad.moviebooking.features.movie;

import kh.edu.istad.moviebooking.domain.Movie;
import kh.edu.istad.moviebooking.domain.enums.MovieStatus;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.common.PageResponse;
import kh.edu.istad.moviebooking.features.movie.dto.MovieResponse;
import kh.edu.istad.moviebooking.features.movie.dto.UpdateMovieStatusRequest;
import kh.edu.istad.moviebooking.intergration.tmdb.TmdbClient;
import kh.edu.istad.moviebooking.intergration.tmdb.dto.search.TmdbMovieDetailResponse;
import kh.edu.istad.moviebooking.mapper.MovieMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(readOnly = true)
    public PageResponse<MovieResponse> getAllMovies(
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        List<String> allowedSortFields = List.of("createdAt", "releaseDate", "title");

        if (!allowedSortFields.contains(sortBy)) {
            sortBy = "createdAt";
        }

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection,sortBy));

        Page<Movie> moviePage = movieRepository.findAll(pageable);

        List<MovieResponse> movies = moviePage
                        .getContent()
                        .stream()
                        .map(movieMapper::toMovieResponse)
                        .toList();

        return new PageResponse<>(
                movies,
                moviePage.getNumber(),
                moviePage.getSize(),
                moviePage.getTotalElements(),
                moviePage.getTotalPages(),
                moviePage.isFirst(),
                moviePage.isLast()
        );
    }

//    get movie by uuid
    @Override
    public MovieResponse getMovieByUuid(UUID uuid) {
        // find movie
        Movie movie = movieRepository.findByUuid(uuid)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Movie",
                                "uuid",
                                uuid
                        )
                );
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
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Movie",
                                "uuid",
                                uuid
                        )
                );
        movie.setStatus(updateMovieStatusRequest.status());

        Movie updatedMovie = movieRepository.save(movie);

        return movieMapper.toMovieResponse(updatedMovie);
    }

    //    delete movie
    @Override
    public void deleteMovieByUuid(UUID uuid) {
        //        find movie
        Movie movie = movieRepository.findByUuid(uuid)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Movie",
                                "uuid",
                                uuid
                        )
                );
        movieRepository.delete(movie);
    }

}
