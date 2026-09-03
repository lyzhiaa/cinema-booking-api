package kh.edu.istad.moviebooking.mapper;

import kh.edu.istad.moviebooking.domain.Seat;
import kh.edu.istad.moviebooking.features.seat.dto.CreateSeatRequest;
import kh.edu.istad.moviebooking.features.seat.dto.SeatResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SeatMapper {
//    get single seat
    @Mapping(
            source = "hall.uuid",
            target = "hallUuid"
    )
    SeatResponse toSeatResponse(Seat seat);
//    get all seats
    List<SeatResponse> toSeatResponseList(List<Seat> seats);
//    create seat
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "hall", ignore = true)
    @Mapping(target = "seatLabel", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Seat fromCreateSeatRequest(
            CreateSeatRequest createSeatRequest
    );
}
