package kh.edu.istad.moviebooking.features.booking;

import kh.edu.istad.moviebooking.features.booking.dto.BookingResponse;
import kh.edu.istad.moviebooking.features.booking.dto.CreateBookingRequest;

import java.util.UUID;

public interface BookingService {
//    create booking
    BookingResponse createBooking(CreateBookingRequest createBookingRequest);

//    get booking by uuid
    BookingResponse getBookingByUuid(UUID bookingUuid);
}
