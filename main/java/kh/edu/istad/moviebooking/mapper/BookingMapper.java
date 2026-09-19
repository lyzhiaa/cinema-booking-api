package kh.edu.istad.moviebooking.mapper;

import kh.edu.istad.moviebooking.domain.Booking;
import kh.edu.istad.moviebooking.domain.BookingSeat;
import kh.edu.istad.moviebooking.features.booking.dto.BookingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = BookingSeatMapper.class
)
public interface BookingMapper {

    @Mapping(source = "booking.showtime.uuid", target = "showtimeUuid")
    @Mapping(source = "booking.showtime.movie.title", target = "movieTitle")
    @Mapping(source = "booking.showtime.hall.name", target = "hallName")
    @Mapping(source = "booking.showtime.startTime", target = "startTime")
    @Mapping(source = "bookingSeats", target = "seats")
    @Mapping(source = "booking.user.uuid", target = "userUuid")
    BookingResponse toBookingResponse(Booking booking, List<BookingSeat> bookingSeats);
}