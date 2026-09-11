package kh.edu.istad.moviebooking.mapper;

import kh.edu.istad.moviebooking.domain.Showtime;
import kh.edu.istad.moviebooking.features.showtime.dto.ShowtimeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShowtimeMapper {

    @Mapping(source = "movie.uuid", target = "movieUuid")
    @Mapping(source = "movie.title", target = "movieTitle")
    @Mapping(source = "hall.uuid", target = "hallUuid")
    @Mapping(source = "hall.name", target = "hallName")
    ShowtimeResponse toShowtimeResponse(Showtime showtime);

    List<ShowtimeResponse> toShowtimeResponseList(List<Showtime> showtimes);
}
