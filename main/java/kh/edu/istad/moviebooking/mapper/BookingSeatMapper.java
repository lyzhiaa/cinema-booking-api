package kh.edu.istad.moviebooking.mapper;

import kh.edu.istad.moviebooking.domain.BookingSeat;
import kh.edu.istad.moviebooking.features.booking.dto.BookingSeatResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingSeatMapper {

    @Mapping(
            source = "seat.uuid",
            target = "seatUuid"
    )
    @Mapping(
            source = "seat.seatLabel",
            target = "seatLabel"
    )
    BookingSeatResponse toBookingSeatResponse(BookingSeat bookingSeat);

    List<BookingSeatResponse> toBookingSeatResponseList(List<BookingSeat> bookingSeats);
}