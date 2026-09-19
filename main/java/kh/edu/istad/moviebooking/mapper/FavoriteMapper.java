package kh.edu.istad.moviebooking.mapper;

import kh.edu.istad.moviebooking.domain.Favorite;
import kh.edu.istad.moviebooking.domain.Movie;
import kh.edu.istad.moviebooking.domain.User;
import kh.edu.istad.moviebooking.features.favorite.dto.FavoriteResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FavoriteMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(source = "user", target = "user")
    @Mapping(source = "movie", target = "movie")
    Favorite fromUserAndMovie(User user, Movie movie);

    @Mapping(source = "movie.uuid", target = "movieUuid")
    @Mapping(source = "movie.title", target = "title")
    @Mapping(source = "movie.posterPath", target = "posterPath")
    @Mapping(source = "movie.releaseDate", target = "releaseDate")
    FavoriteResponse toFavoriteResponse(Favorite favorite);

    List<FavoriteResponse> toFavoriteResponseList(List<Favorite> favorites);
}
