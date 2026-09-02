package kh.edu.istad.moviebooking.mapper;

import kh.edu.istad.moviebooking.domain.Movie;
import kh.edu.istad.moviebooking.features.movie.dto.MovieResponse;
import kh.edu.istad.moviebooking.features.movie.dto.search.MovieSearchResponse;
import kh.edu.istad.moviebooking.intergration.tmdb.dto.TmdbMovieSearchItem;
import kh.edu.istad.moviebooking.intergration.tmdb.dto.search.TmdbMovieDetailResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MovieMapper {
    MovieSearchResponse fromTmdbSearch(TmdbMovieSearchItem tmdbMovieSearchItem);
//    get movie
    @Mapping(
        source = "posterPath",
        target = "posterUrl",
        qualifiedByName = "toPosterUrl"
    )
    @Mapping(
        source = "backdropPath",
        target = "backdropUrl",
        qualifiedByName = "toBackdropUrl"
    )
    MovieResponse toMovieResponse(Movie movie);
//    get all movies
    List<MovieResponse> toMovieResponseList(List<Movie> movies);
//    import movie from TMDB
    @Mapping(source = "id", target = "tmdbId")
    @Mapping(source = "runtime", target = "runtimeMinutes")
    @Mapping(source = "originalLanguage", target = "language")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ageRating", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Movie fromTmdbDetail(TmdbMovieDetailResponse source);
    default LocalDate map(String date) {

        if (date == null || date.isBlank()) {
            return null;
        }

        return LocalDate.parse(date);
    }
    @Named("toPosterUrl")
    default String toPosterUrl(String path) {

        if (path == null || path.isBlank()) {
            return null;
        }

        return "https://image.tmdb.org/t/p/w500" + path;
    }

    @Named("toBackdropUrl")
    default String toBackdropUrl(String path) {

        if (path == null || path.isBlank()) {
            return null;
        }

        return "https://image.tmdb.org/t/p/w1280" + path;
    }
}
